package com.example.scancaptureapp.utils

import android.content.Context
import android.net.Uri
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.core.content.FileProvider
import com.example.scancaptureapp.R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageFileManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val scansDir: File
        get() = File(context.filesDir, "scans").also { it.mkdirs() }

    fun createImageFile(): File {
        val fileName = "scan_${System.currentTimeMillis()}.jpg"
        return File(scansDir, fileName)
    }

    /**
     * Cache dir capture file — safe for CameraX without storage permissions.
     */
    fun createCacheCaptureFile(): File {
        val cacheScans = File(context.cacheDir, "camera_captures").also { it.mkdirs() }
        return File(cacheScans, "capture_${System.currentTimeMillis()}.jpg")
    }

    fun copyUriToCache(uri: Uri): File {
        val dest = createCacheCaptureFile()
        context.contentResolver.openInputStream(uri)?.use { input ->
            dest.outputStream().use { output -> input.copyTo(output) }
        } ?: throw IllegalStateException(context.getString(R.string.error_cannot_read_gallery_image))
        return dest
    }

    fun getUriForFile(file: File): Uri =
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

    fun saveBitmap(bitmap: Bitmap, file: File): Uri {
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        return getUriForFile(file)
    }

    /**
     * Crops the source image to the given normalized rect (0..1) and saves to a new file.
     */
    fun cropImage(sourcePath: String, left: Float, top: Float, right: Float, bottom: Float): File {
        val source = BitmapFactory.decodeFile(sourcePath)
            ?: throw IllegalStateException(context.getString(R.string.error_failed_load_image_crop))
        val x = (left * source.width).toInt().coerceIn(0, source.width - 1)
        val y = (top * source.height).toInt().coerceIn(0, source.height - 1)
        val w = ((right - left) * source.width).toInt().coerceIn(1, source.width - x)
        val h = ((bottom - top) * source.height).toInt().coerceIn(1, source.height - y)
        val cropped = Bitmap.createBitmap(source, x, y, w, h)
        source.recycle()
        val outFile = createImageFile()
        saveBitmap(cropped, outFile)
        cropped.recycle()
        return outFile
    }

    fun rotateBitmapIfNeeded(sourcePath: String, degrees: Int): File {
        if (degrees == 0) return File(sourcePath)
        val source = BitmapFactory.decodeFile(sourcePath) ?: return File(sourcePath)
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        val rotated = Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        source.recycle()
        val outFile = createImageFile()
        saveBitmap(rotated, outFile)
        rotated.recycle()
        return outFile
    }
}
