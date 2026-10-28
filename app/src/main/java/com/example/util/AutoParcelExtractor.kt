package com.example.util

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.regex.Pattern
import kotlin.coroutines.resume

data class ExtractedParcelData(
    val price: Long,
    val recipientPhone: String,
    val deliveryAddress: String,
    val recipientName: String = "",
    val sellerName: String = "",
    val notes: String = "",
    val rawExtractedTokens: List<String> = emptyList()
)

object AutoParcelExtractor {

    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    /**
     * Automatically extracts parcel information from a Bitmap using on-device ML Kit OCR
     * and strictly extracts only what is found in the image. No hardcoded fake data!
     */
    suspend fun extractFromBitmap(bitmap: Bitmap): ExtractedParcelData =
        suspendCancellableCoroutine { continuation ->
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            recognizer.process(inputImage)
                .addOnSuccessListener { visionText ->
                    val rawText = visionText.text
                    val parsed = parseCourierLabelText(rawText)
                    continuation.resume(parsed)
                }
                .addOnFailureListener {
                    continuation.resume(
                        ExtractedParcelData(
                            price = 0L,
                            recipientPhone = "",
                            deliveryAddress = "",
                            recipientName = "",
                            sellerName = "",
                            notes = "",
                            rawExtractedTokens = emptyList()
                        )
                    )
                }
        }

    /**
     * Parses the OCR text into structured courier parcel fields:
     * Price, Phone number, Address, and Notes.
     */
    fun parseCourierLabelText(rawText: String): ExtractedParcelData {
        val normalizedText = normalizeDigits(rawText)
        val lines = normalizedText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val allTokens = normalizedText.split("\\s+".toRegex()).map { it.trim() }.filter { it.length >= 2 }

        var detectedPrice: Long = 0L
        var detectedPhone = ""
        var detectedAddress = ""
        var detectedSeller = ""
        var detectedNotes = ""

        // Known courier hotlines to exclude (e.g. Postyar hotline on sticker/bags)
        val blacklistedHotlines = setOf(
            "07704753999", "07701463200", "07701463201", "07701463202",
            "07701463203", "07701463204", "07701463205", "07701463206",
            "07701463207", "07701463208", "07701463209"
        )

        // 1. Phone number detection (Iraqi mobile: 075x, 077x, 078x, 079x - 10 to 11 digits)
        val phonePattern = Pattern.compile("07[5789]\\d{8}")
        
        // Priority 1: Check lines explicitly labeled with customer / recipient keywords
        for (line in lines) {
            val lower = line.lowercase()
            val isCustomerLine = lower.contains("کڕیار") || lower.contains("وەرگر") ||
                    lower.contains("مۆبایل") || lower.contains("تەلەفۆن") ||
                    lower.contains("customer") || lower.contains("receiver") ||
                    lower.contains("phone") || lower.contains("mobile")
            
            // Skip company footer / seller lines
            val isCompanyOrSeller = lower.contains("فرۆشیار") || lower.contains("پۆستیار") ||
                    lower.contains("کۆمپانیا") || lower.contains("هەڵبژاردن") ||
                    lower.contains("سوپاس") || lower.contains("postyar")

            if (isCustomerLine && !isCompanyOrSeller) {
                val cleaned = line.replace(" ", "").replace("-", "").replace("/", "")
                val m = phonePattern.matcher(cleaned)
                if (m.find()) {
                    val p = m.group()
                    if (p !in blacklistedHotlines) {
                        detectedPhone = p
                        break
                    }
                }
            }
        }

        // Priority 2: If not found near label, check all lines excluding seller and courier brand lines
        if (detectedPhone.isEmpty()) {
            for (line in lines) {
                val lower = line.lowercase()
                val isCompanyOrSeller = lower.contains("فرۆشیار") || lower.contains("پۆستیار") ||
                        lower.contains("کۆمپانیا") || lower.contains("هەڵبژاردن") ||
                        lower.contains("سوپاس") || lower.contains("postyar") ||
                        lower.contains("گە یاندمان") || lower.contains("کوردستان")
                
                if (!isCompanyOrSeller) {
                    val cleaned = line.replace(" ", "").replace("-", "")
                    val m = phonePattern.matcher(cleaned)
                    if (m.find()) {
                        val p = m.group()
                        if (p !in blacklistedHotlines) {
                            detectedPhone = p
                            break
                        }
                    }
                }
            }
        }

        // 2. Price detection (numbers like 47000, 25000, 10000, 45000)
        // First check lines containing price keywords
        val priceKeywordPattern = Pattern.compile("\\b(\\d{4,6})\\b")
        for (line in lines) {
            val lower = line.lowercase()
            if (lower.contains("iqd") || lower.contains("نرخ") || lower.contains("قیمت") ||
                lower.contains("سعر") || lower.contains("د.ع") || lower.contains("گەیاندن")
            ) {
                val m = priceKeywordPattern.matcher(line.replace(",", "").replace(".", "").replace(":", " "))
                while (m.find()) {
                    val p = m.group().toLongOrNull() ?: 0L
                    if (p in 3000L..900000L && p.toString() != detectedPhone && !detectedPhone.contains(p.toString())) {
                        detectedPrice = p
                        break
                    }
                }
            }
            if (detectedPrice > 0L) break
        }

        // If not found near keywords, look for standalone amounts (typically ending in 000 or 500)
        if (detectedPrice == 0L) {
            for (token in allTokens) {
                val cleaned = token.replace(",", "").replace(".", "")
                if (cleaned.length in 4..6 && cleaned.all { it.isDigit() }) {
                    val candidate = cleaned.toLongOrNull() ?: 0L
                    // Avoid picking any phone number or phone prefix
                    if (candidate in 3000L..900000L && !detectedPhone.contains(cleaned)) {
                        if (candidate % 250 == 0L || candidate % 1000 == 0L) {
                            detectedPrice = candidate
                            break
                        }
                    }
                }
            }
        }

        // 3. Address detection
        val knownCities = listOf(
            "هەولێر", "سلێمانی", "دهۆک", "کەرکووک", "زاخۆ", "ڕانیە", "کۆیە", "سۆران",
            "مام زاوا", "مام زاوە", "بەحرکە", "عەنکاوە", "شۆڕش", "ڕاستی", "باداوە", "کەسنەزان",
            "هەڵەبجە", "کەلار", "کفری", "چەمچەماڵ", "ئاکرێ", "سێمێل", "Erbil", "Hawler",
            "Sulaymaniyah", "Slemani", "Duhok", "Kirkuk"
        )

        for (line in lines) {
            if (line.contains("ناونیشان") || line.contains("عنوان") || line.contains("Address")) {
                val clean = line.replace("ناونیشان", "")
                    .replace(":", "")
                    .replace("عنوان", "")
                    .replace("Address", "")
                    .trim()
                if (clean.length > 2) {
                    detectedAddress = clean
                    break
                }
            }
            for (city in knownCities) {
                if (line.contains(city)) {
                    val clean = line.replace("ناونیشان", "").replace(":", "").trim()
                    detectedAddress = clean
                    break
                }
            }
            if (detectedAddress.isNotEmpty()) break
        }

        // 4. Seller & Notes detection
        for (line in lines) {
            if (line.contains("فرۆشیار") || line.contains("Seller")) {
                val clean = line.replace("فرۆشیار", "").replace("ناوی", "").replace("ژمارەی", "").replace(":", "").trim()
                if (clean.length > 2) detectedSeller = clean
            }
            if (line.contains("تێبینی") || line.contains("Note")) {
                val clean = line.replace("تێبینی", "").replace(":", "").trim()
                if (clean.length > 2) detectedNotes = clean
            }
        }

        return ExtractedParcelData(
            price = detectedPrice,
            recipientPhone = detectedPhone,
            deliveryAddress = detectedAddress,
            recipientName = if (detectedPhone.isNotEmpty()) "کڕیار" else "",
            sellerName = detectedSeller,
            notes = detectedNotes,
            rawExtractedTokens = allTokens
        )
    }

    private fun normalizeDigits(input: String): String {
        val arabic = "٠١٢٣٤٥٦٧٨٩"
        val persian = "۰۱۲۳۴۵۶۷۸۹"
        var out = input
        for (i in 0..9) {
            out = out.replace(arabic[i], ('0' + i))
            out = out.replace(persian[i], ('0' + i))
        }
        return out
    }
}
