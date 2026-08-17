package com.noxtope.jam.ui.theme

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream

// Decodifica una imagen reduciendo su tamaño con inSampleSize para evitar
// OutOfMemoryError con fotos de alta resolución (12MP+).
fun decodeSampledBitmap(context: Context, uri: Uri, maxWidth: Int, maxHeight: Int): Bitmap? {
    return try {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        }
        var sampleSize = 1
        while (
            (bounds.outWidth / sampleSize) > maxWidth * 2 ||
            (bounds.outHeight / sampleSize) > maxHeight * 2
        ) {
            sampleSize *= 2
        }
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }
    } catch (e: Exception) {
        null
    }
}

fun bitmapToBase64(bitmap: Bitmap, maxWidth: Int, quality: Int = 70): String {
    return try {
        val resized = if (bitmap.width > maxWidth) {
            val ratio = maxWidth.toFloat() / bitmap.width.toFloat()
            Bitmap.createScaledBitmap(bitmap, maxWidth, (bitmap.height * ratio).toInt(), true)
        } else bitmap
        val outputStream = ByteArrayOutputStream()
        resized.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
    } catch (e: Exception) {
        ""
    }
}
