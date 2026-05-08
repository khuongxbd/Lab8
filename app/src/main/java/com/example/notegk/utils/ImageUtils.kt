package com.example.notegk.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream

object ImageUtils {

    // Nen anh va ma hoa Base64 de luu truc tiep len Firestore
    fun uriToBase64(context: Context, uri: Uri, maxBytes: Int = 1024 * 1024): String {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Khong the mo input stream tu Uri")
        val originalBytes = inputStream.use { it.readBytes() }
        val originalBitmap = BitmapFactory.decodeByteArray(originalBytes, 0, originalBytes.size)
            ?: throw IllegalArgumentException("Khong decode duoc bitmap")

        val resized = resizeIfNeeded(originalBitmap, 1280)
        val compressedBytes = compressToTarget(resized, maxBytes)

        return Base64.encodeToString(compressedBytes, Base64.NO_WRAP)
    }

    // Giai ma Base64 thanh Bitmap de hien thi tren Compose
    fun base64ToBitmap(base64: String): Bitmap? {
        return try {
            if (base64.isBlank()) return null
            val decoded = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
        } catch (_: Exception) {
            null
        }
    }

    private fun resizeIfNeeded(bitmap: Bitmap, maxSide: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val maxCurrent = maxOf(width, height)
        if (maxCurrent <= maxSide) return bitmap

        val ratio = maxSide.toFloat() / maxCurrent.toFloat()
        val newWidth = (width * ratio).toInt().coerceAtLeast(1)
        val newHeight = (height * ratio).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun compressToTarget(bitmap: Bitmap, maxBytes: Int): ByteArray {
        var quality = 95
        var result = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, result)

        while (result.size() > maxBytes && quality > 10) {
            quality -= 5
            result = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, result)
        }

        // Neu van lon hon 1MB thi tiep tuc thu giam kich thuoc bitmap nhe
        var currentBitmap = bitmap
        while (result.size() > maxBytes) {
            val reducedWidth = (currentBitmap.width * 0.9f).toInt().coerceAtLeast(1)
            val reducedHeight = (currentBitmap.height * 0.9f).toInt().coerceAtLeast(1)
            if (reducedWidth == currentBitmap.width && reducedHeight == currentBitmap.height) break
            currentBitmap = Bitmap.createScaledBitmap(currentBitmap, reducedWidth, reducedHeight, true)
            quality = 85
            result = ByteArrayOutputStream()
            currentBitmap.compress(Bitmap.CompressFormat.JPEG, quality, result)
            while (result.size() > maxBytes && quality > 10) {
                quality -= 5
                result = ByteArrayOutputStream()
                currentBitmap.compress(Bitmap.CompressFormat.JPEG, quality, result)
            }
            if (reducedWidth <= 64 || reducedHeight <= 64) break
        }

        return result.toByteArray()
    }
}
