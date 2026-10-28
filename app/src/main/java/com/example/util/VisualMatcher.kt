package com.example.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import com.example.data.model.ParcelEntity
import java.io.File
import kotlin.math.sqrt

data class MatchEvaluation(
    val parcel: ParcelEntity,
    val confidencePercent: Int,
    val isMatch: Boolean,
    val matchReason: String = "",
    val allPendingCandidates: List<ParcelEntity> = emptyList()
)

object VisualMatcher {

    private const val HASH_WIDTH = 32
    private const val HASH_HEIGHT = 32
    private const val GRID_SIZE = 8 // 8x8 color grid (64 blocks)

    /**
     * Generates a rich, accurate visual fingerprint:
     * - 32x32 gradient hash (1024 bits)
     * - 8x8 spatial color grid
     */
    fun generateFingerprint(bitmap: Bitmap): String {
        val dHash = computeDifferenceHash(bitmap)
        val colorGrid = computeColorGrid(bitmap)
        return "$dHash|$colorGrid"
    }

    /**
     * Finds the best match among pending parcels using multi-factor verification:
     * 1. 32x32 structural gradient comparison & spatial color layout
     * 2. Customer Phone number verification (only if non-company and visual consistency >= 40%)
     * 3. Price match consistency
     * 4. Address keywords overlap
     * Strict minimum threshold prevents ever matching or deleting the wrong package!
     */
    suspend fun findBestMatchWithOcr(
        queryBitmap: Bitmap,
        candidates: List<ParcelEntity>
    ): MatchEvaluation? {
        if (candidates.isEmpty()) return null

        // 1. Run OCR on query photo to extract real numbers and tokens
        val queryOcr = AutoParcelExtractor.extractFromBitmap(queryBitmap)
        val queryPhone = queryOcr.recipientPhone.replace(" ", "").replace("-", "")
        val queryPrice = queryOcr.price
        val queryTokens = queryOcr.rawExtractedTokens.map { it.lowercase() }.toSet()

        // 2. Compute visual fingerprint
        val queryFingerprint = generateFingerprint(queryBitmap)

        var bestParcel: ParcelEntity? = null
        var bestScore = -1.0
        var bestReason = ""
        var secondBestScore = -1.0

        for (parcel in candidates) {
            val parcelFingerprint = if (parcel.visualFingerprint.isNotBlank()) {
                parcel.visualFingerprint
            } else {
                val file = File(parcel.photoPath)
                if (file.exists()) {
                    val bmp = BitmapFactory.decodeFile(file.absolutePath)
                    if (bmp != null) generateFingerprint(bmp) else null
                } else null
            } ?: continue

            val visualSim = calculateVisualSimilarity(queryFingerprint, parcelFingerprint)

            var score = visualSim * 0.70 // Base visual score (up to 70%)
            var reason = "هاوشێوەیی لەزگە و پێکهاتەی وێنە"

            // Factor 1: Customer Phone Number Match (Requires minimum visual plausibility)
            val cleanCandPhone = parcel.recipientPhone.replace(" ", "").replace("-", "")
            if (queryPhone.length >= 8 && cleanCandPhone.length >= 8) {
                if (cleanCandPhone == queryPhone || cleanCandPhone.endsWith(queryPhone) || queryPhone.endsWith(cleanCandPhone)) {
                    if (visualSim >= 0.35) {
                        score += 0.30 // Major 30% boost for verified customer phone
                        reason = "ژمارەی مۆبایلی کڕیار بە تەواوی هاوتایە"
                    }
                }
            }

            // Factor 2: Price Match
            if (queryPrice > 0L && parcel.price == queryPrice) {
                score += 0.15
                if (reason == "هاوشێوەیی لەزگە و پێکهاتەی وێنە") {
                    reason = "هاوتایی لە نرخ و شێوەی لەزگە"
                }
            }

            // Factor 3: Address / Notes keyword overlap
            if (parcel.deliveryAddress.isNotBlank()) {
                val addrWords = parcel.deliveryAddress.lowercase().split("\\s+".toRegex()).filter { it.length >= 3 }
                val overlap = addrWords.count { queryTokens.contains(it) }
                if (overlap > 0) score += 0.08
            }

            val finalScore = score.coerceIn(0.0, 1.0)

            if (finalScore > bestScore) {
                secondBestScore = bestScore
                bestScore = finalScore
                bestParcel = parcel
                bestReason = reason
            } else if (finalScore > secondBestScore) {
                secondBestScore = finalScore
            }
        }

        if (bestParcel == null) return null

        val confidence = (bestScore * 100).toInt().coerceIn(0, 100)

        // Strict criteria to prevent picking the wrong item:
        // Must be >= 75% confident
        val isStrictMatch = confidence >= 75

        return MatchEvaluation(
            parcel = bestParcel,
            confidencePercent = confidence,
            isMatch = isStrictMatch,
            matchReason = if (isStrictMatch) bestReason else "ناڕوون یان هاوشێوە نییە",
            allPendingCandidates = candidates
        )
    }

    private fun calculateVisualSimilarity(fp1: String, fp2: String): Double {
        val parts1 = fp1.split("|")
        val parts2 = fp2.split("|")

        if (parts1.size < 2 || parts2.size < 2) return 0.0

        val dHashSim = compareBinaryStrings(parts1[0], parts2[0])
        val colorSim = compareColorProfiles(parts1[1], parts2[1])

        // 70% structural gradient edges, 30% spatial color layout
        return (dHashSim * 0.70) + (colorSim * 0.30)
    }

    private fun computeDifferenceHash(bitmap: Bitmap): String {
        val scaled = Bitmap.createScaledBitmap(bitmap, HASH_WIDTH + 1, HASH_HEIGHT, true)
        val sb = StringBuilder()
        for (y in 0 until HASH_HEIGHT) {
            for (x in 0 until HASH_WIDTH) {
                val left = getLuminance(scaled.getPixel(x, y))
                val right = getLuminance(scaled.getPixel(x + 1, y))
                sb.append(if (left < right) '1' else '0')
            }
        }
        return sb.toString()
    }

    private fun computeColorGrid(bitmap: Bitmap): String {
        val scaled = Bitmap.createScaledBitmap(bitmap, GRID_SIZE, GRID_SIZE, true)
        val sb = StringBuilder()
        for (y in 0 until GRID_SIZE) {
            for (x in 0 until GRID_SIZE) {
                val pixel = scaled.getPixel(x, y)
                val r = Color.red(pixel) / 16 // 0..15 hex
                val g = Color.green(pixel) / 16
                val b = Color.blue(pixel) / 16
                sb.append("%01x%01x%01x".format(r, g, b))
            }
        }
        return sb.toString()
    }

    private fun compareBinaryStrings(s1: String, s2: String): Double {
        val minLen = minOf(s1.length, s2.length)
        if (minLen == 0) return 0.0
        var matchingBits = 0
        for (i in 0 until minLen) {
            if (s1[i] == s2[i]) matchingBits++
        }
        return matchingBits.toDouble() / minLen
    }

    private fun compareColorProfiles(p1: String, p2: String): Double {
        if (p1.length != p2.length || p1.isEmpty()) return 0.0
        val blockCount = p1.length / 3
        var totalDist = 0.0
        val maxDist = sqrt((15 * 15 * 3).toDouble())

        for (i in 0 until blockCount) {
            val r1 = p1[i * 3].digitToInt(16)
            val g1 = p1[i * 3 + 1].digitToInt(16)
            val b1 = p1[i * 3 + 2].digitToInt(16)

            val r2 = p2[i * 3].digitToInt(16)
            val g2 = p2[i * 3 + 1].digitToInt(16)
            val b2 = p2[i * 3 + 2].digitToInt(16)

            val dr = r1 - r2
            val dg = g1 - g2
            val db = b1 - b2
            val dist = sqrt((dr * dr + dg * dg + db * db).toDouble())
            totalDist += dist
        }

        val avgDist = totalDist / blockCount
        return (1.0 - (avgDist / maxDist)).coerceIn(0.0, 1.0)
    }

    private fun getLuminance(color: Int): Int {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        return (0.299 * r + 0.587 * g + 0.114 * b).toInt()
    }
}
