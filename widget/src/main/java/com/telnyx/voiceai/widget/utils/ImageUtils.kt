package com.telnyx.voiceai.widget.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.InputStream

/**
 * Utility class for image processing and conversion
 */
object ImageUtils {
    
    /**
     * Convert image URI to base64 encoded string
     */
    fun uriToBase64(context: Context, uri: Uri, maxWidth: Int = 800, maxHeight: Int = 600): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            inputStream?.use { stream ->
                val originalBitmap = BitmapFactory.decodeStream(stream)
                val resizedBitmap = resizeBitmap(originalBitmap, maxWidth, maxHeight)
                val outputStream = ByteArrayOutputStream()
                
                // Compress as JPEG with 80% quality
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                val byteArray = outputStream.toByteArray()
                
                // Create data URL with base64 encoding
                "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.NO_WRAP)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Convert base64 encoded string to Bitmap
     * Supports both raw base64 strings and data URL format (e.g., "data:image/jpeg;base64,...")
     */
    fun base64ToBitmap(base64String: String): Bitmap? {
        return try {
            // Remove data URL prefix if present (e.g., "data:image/jpeg;base64,")
            val base64Data = if (base64String.contains(",")) {
                base64String.substring(base64String.indexOf(",") + 1)
            } else {
                base64String
            }

            // Decode base64 string to byte array
            val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)

            // Convert byte array to Bitmap
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Resize bitmap to fit within specified dimensions while maintaining aspect ratio
     */
    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }
        
        val aspectRatio = width.toFloat() / height.toFloat()
        
        val (newWidth, newHeight) = if (aspectRatio > 1) {
            // Landscape
            val w = minOf(maxWidth, width)
            val h = (w / aspectRatio).toInt()
            w to h
        } else {
            // Portrait or square
            val h = minOf(maxHeight, height)
            val w = (h * aspectRatio).toInt()
            w to h
        }
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
}
