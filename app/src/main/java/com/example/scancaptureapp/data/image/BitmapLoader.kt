package com.example.scancaptureapp.data.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayInputStream
import java.io.File
import kotlin.math.max

object BitmapLoader {

    private const val MAX_OCR_DIMENSION = 2400
    private const val MIN_OCR_DIMENSION = 900

    fun decodeFromPath(path: String): Bitmap {
        val sampled = decodeSampled(path)
        return applyExifRotation(path, sampled)
    }

    fun decodeFromUri(context: Context, uri: Uri): Bitmap {
        val path = uri.path
        if (path != null && File(path).exists()) {
            return decodeFromPath(path)
        }
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalStateException("Cannot open image stream")
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        val options = BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight)
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
            ?: throw IllegalStateException("Failed to decode image")
        val rotation = try {
            val exif = ExifInterface(ByteArrayInputStream(bytes))
            exifRotationDegrees(exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL))
        } catch (_: Exception) {
            0
        }
        return rotateIfNeeded(bitmap, rotation)
    }

    private fun decodeSampled(path: String): Bitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        val options = BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight)
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        return BitmapFactory.decodeFile(path, options)
            ?: throw IllegalStateException("Failed to decode image")
    }

    private fun applyExifRotation(path: String, bitmap: Bitmap): Bitmap {
        val rotation = try {
            val exif = ExifInterface(path)
            exifRotationDegrees(exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL))
        } catch (_: Exception) {
            0
        }
        return rotateIfNeeded(bitmap, rotation)
    }

    private fun exifRotationDegrees(orientation: Int): Int = when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90
        ExifInterface.ORIENTATION_ROTATE_180 -> 180
        ExifInterface.ORIENTATION_ROTATE_270 -> 270
        else -> 0
    }

    private fun rotateIfNeeded(bitmap: Bitmap, degrees: Int): Bitmap {
        if (degrees == 0) return ensureMinSize(bitmap)
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (rotated != bitmap) bitmap.recycle()
        return ensureMinSize(rotated)
    }

    private fun ensureMinSize(bitmap: Bitmap): Bitmap {
        val maxSide = max(bitmap.width, bitmap.height)
        if (maxSide >= MIN_OCR_DIMENSION) return bitmap
        val scale = MIN_OCR_DIMENSION.toFloat() / maxSide
        val w = (bitmap.width * scale).toInt()
        val h = (bitmap.height * scale).toInt()
        val scaled = Bitmap.createScaledBitmap(bitmap, w, h, true)
        if (scaled != bitmap) bitmap.recycle()
        return scaled
    }

    private fun calculateInSampleSize(width: Int, height: Int): Int {
        var inSampleSize = 1
        val maxDim = max(width, height)
        while (maxDim / inSampleSize > MAX_OCR_DIMENSION) {
            inSampleSize *= 2
        }
        return inSampleSize.coerceAtLeast(1)
    }

    fun scaleDownForBlurCheck(bitmap: Bitmap, maxSide: Int = 480): Bitmap {
        val side = max(bitmap.width, bitmap.height)
        if (side <= maxSide) return bitmap.copy(bitmap.config ?: Bitmap.Config.ARGB_8888, true)
        val scale = maxSide.toFloat() / side
        val w = max(1, (bitmap.width * scale).toInt())
        val h = max(1, (bitmap.height * scale).toInt())
        return Bitmap.createScaledBitmap(bitmap, w, h, true)
    }
}
