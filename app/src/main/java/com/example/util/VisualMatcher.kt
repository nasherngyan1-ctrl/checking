package com.example.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import com.example.data.model.ParcelEntity
import java.io.File
import kotlin.math.abs
import kotlin.math.sqrt

data class MatchEvaluation(
    val parcel: ParcelEntity,
    val confidencePercent: Int,
    val isMatch: Boolean
)

object VisualMatcher {

    private const val HASH_WIDTH = 16
    private const val HASH_HEIGHT = 16
    private const val GRID_SIZE = 4 // 4x4 color grid

    /**
     * Extracts a compact visual fingerprint string from a Bitmap.
     * Contains:
     * 1. 240-bit dHash (hex string)
     * 2. 64-bit aHash (hex string)
     * 3. 4x4 spatial color profile (encoded)
     */
    fun generateFingerprint(bitmap: Bitmap): String {
        val dHash = computeDifferenceHash(bitmap)
        val aHash = computeAverageHash(bitmap)
        val colorProfile = computeColorGrid(bitmap)
        return "$dHash|$aHash|$colorProfile"
    }

    /**
     * Compares a query bitmap against a list of saved parcels.
     * Returns the best match evaluation.
     */
    fun findBestMatch(
        queryBitmap: Bitmap,
        candidates: List<ParcelEntity>,
        minConfidenceThreshold: Int = 60
    ): MatchEvaluation? {
        if (candidates.isEmpty()) return null

        val queryFingerprint = generateFingerprint(queryBitmap)

        var bestParcel: ParcelEntity? = null
        var bestScore = -1.0

        for (parcel in candidates) {
            val parcelFingerprint = if (parcel.visualFingerprint.isNotBlank()) {
                parcel.visualFingerprint
            } else {
                // If fingerprint wasn't cached, generate from file
                val file = File(parcel.photoPath)
                if (file.exists()) {
                    val bmp = BitmapFactory.decodeFile(file.absolutePath)
                    if (bmp != null) generateFingerprint(bmp) else null
                } else null
            } ?: continue

            val score = calculateSimilarity(queryFingerprint, parcelFingerprint)
            if (score > bestScore) {
                bestScore = score
                bestParcel = parcel
            }
        }

        if (bestParcel == null) return null

        val confidence = (bestScore * 100).toInt().coerceIn(0, 100)
        val isMatch = confidence >= minConfidenceThreshold

        return MatchEvaluation(
            parcel = bestParcel,
            confidencePercent = confidence,
            isMatch = isMatch
        )
    }

    /**
     * Calculates overall similarity between 0.0 and 1.0
     */
    fun calculateSimilarity(fp1: String, fp2: String): Double {
        val parts1 = fp1.split("|")
        val parts2 = fp2.split("|")

        if (parts1.size < 3 || parts2.size < 3) return 0.0

        val dHashSim = compareBinaryStrings(parts1[0], parts2[0])
        val aHashSim = compareBinaryStrings(parts1[1], parts2[1])
        val colorSim = compareColorProfiles(parts1[2], parts2[2])

        // dHash and aHash capture edge & intensity structure (70% weight)
        // color grid captures package/label color regions (30% weight)
        return (dHashSim * 0.45) + (aHashSim * 0.25) + (colorSim * 0.30)
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

    private fun computeAverageHash(bitmap: Bitmap): String {
        val scaled = Bitmap.createScaledBitmap(bitmap, 8, 8, true)
        var sum = 0L
        val luminances = IntArray(64)
        var idx = 0
        for (y in 0 until 8) {
            for (x in 0 until 8) {
                val lum = getLuminance(scaled.getPixel(x, y))
                luminances[idx++] = lum
                sum += lum
            }
        }
        val avg = sum / 64
        val sb = StringBuilder()
        for (lum in luminances) {
            sb.append(if (lum >= avg) '1' else '0')
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
        val similarity = (1.0 - (avgDist / maxDist)).coerceIn(0.0, 1.0)
        return similarity
    }

    private fun getLuminance(color: Int): Int {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        return (0.299 * r + 0.587 * g + 0.114 * b).toInt()
    }
}
