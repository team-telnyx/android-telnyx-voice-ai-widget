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
    
    /**
     * Check if the URI is a valid image
     */
    fun isValidImageUri(context: Context, uri: Uri): Boolean {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            inputStream?.use { stream ->
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeStream(stream, null, options)
                options.outWidth > 0 && options.outHeight > 0
            } ?: false
        } catch (e: Exception) {
            false
        }
    }
}