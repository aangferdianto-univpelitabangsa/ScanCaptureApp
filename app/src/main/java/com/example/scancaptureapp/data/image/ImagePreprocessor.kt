package com.example.scancaptureapp.data.image

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Canvas as AndroidCanvas
import com.example.scancaptureapp.domain.model.ScanMode
import com.example.scancaptureapp.domain.repository.ImageProcessor
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Singleton
class ImagePreprocessor @Inject constructor() : ImageProcessor {

    override fun processForOcr(source: Bitmap, scanMode: ScanMode, enhance: Boolean): Bitmap {
        if (!enhance) return source.copy(source.config ?: Bitmap.Config.ARGB_8888, true)

        var working = toGrayscale(source)
        if (working != source) source.recycle()

        working = denoiseMedian(working, radius = when (scanMode) {
            ScanMode.HANDWRITING -> 1
            else -> 2
        })
        working = adjustContrast(working, strength = when (scanMode) {
            ScanMode.RECEIPT -> 1.35f
            ScanMode.HANDWRITING -> 1.15f
            ScanMode.DOCUMENT -> 1.28f
        })

        if (scanMode != ScanMode.HANDWRITING) {
            val thresholded = adaptiveThreshold(working, blockSize = when (scanMode) {
                ScanMode.RECEIPT -> 15
                ScanMode.DOCUMENT -> 21
                else -> 21
            })
            working.recycle()
            working = thresholded
        }

        working = sharpen(working)
        val deskewed = deskew(working)
        if (deskewed != working) working.recycle()
        return deskewed
    }

    override fun detectBlurScore(source: Bitmap): Double {
        val small = BitmapLoader.scaleDownForBlurCheck(source)
        val gray = toGrayscale(small)
        if (small != source) small.recycle()

        val w = gray.width
        val h = gray.height
        val pixels = IntArray(w * h)
        gray.getPixels(pixels, 0, w, 0, 0, w, h)
        gray.recycle()

        var sum = 0.0
        var sumSq = 0.0
        var count = 0
        for (y in 1 until h - 1) {
            for (x in 1 until w - 1) {
                val c = Color.red(pixels[y * w + x])
                val lap = (-4 * c +
                    Color.red(pixels[(y - 1) * w + x]) +
                    Color.red(pixels[(y + 1) * w + x]) +
                    Color.red(pixels[y * w + (x - 1)]) +
                    Color.red(pixels[y * w + (x + 1)])).toDouble()
                sum += lap
                sumSq += lap * lap
                count++
            }
        }
        if (count == 0) return 0.0
        val mean = sum / count
        val variance = (sumSq / count) - (mean * mean)
        return max(0.0, variance)
    }

    private fun toGrayscale(bitmap: Bitmap): Bitmap {
        val out = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = AndroidCanvas(out)
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
        }
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return out
    }

    private fun adjustContrast(bitmap: Bitmap, strength: Float): Bitmap {
        val scale = strength
        val translate = 128f * (1f - scale)
        val matrix = ColorMatrix(
            floatArrayOf(
                scale, 0f, 0f, 0f, translate,
                0f, scale, 0f, 0f, translate,
                0f, 0f, scale, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            )
        )
        val out = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = AndroidCanvas(out)
        val paint = Paint().apply { colorFilter = ColorMatrixColorFilter(matrix) }
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return out
    }

    private fun denoiseMedian(bitmap: Bitmap, radius: Int): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
        val out = IntArray(w * h)
        for (y in 0 until h) {
            for (x in 0 until w) {
                val neighbors = ArrayList<Int>((2 * radius + 1) * (2 * radius + 1))
                for (dy in -radius..radius) {
                    for (dx in -radius..radius) {
                        val nx = (x + dx).coerceIn(0, w - 1)
                        val ny = (y + dy).coerceIn(0, h - 1)
                        neighbors.add(Color.red(pixels[ny * w + nx]))
                    }
                }
                neighbors.sort()
                val median = neighbors[neighbors.size / 2]
                out[y * w + x] = Color.rgb(median, median, median)
            }
        }
        val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        result.setPixels(out, 0, w, 0, 0, w, h)
        return result
    }

    private fun adaptiveThreshold(bitmap: Bitmap, blockSize: Int): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
        val gray = IntArray(w * h) { i -> Color.red(pixels[i]) }
        val out = IntArray(w * h)
        val half = blockSize / 2
        val integral = buildIntegralImage(gray, w, h)

        for (y in 0 until h) {
            for (x in 0 until w) {
                val x1 = max(0, x - half)
                val y1 = max(0, y - half)
                val x2 = min(w - 1, x + half)
                val y2 = min(h - 1, y + half)
                val count = (x2 - x1 + 1) * (y2 - y1 + 1)
                val sum = regionSum(integral, w, x1, y1, x2, y2)
                val mean = sum / count
                val threshold = mean - 10
                val value = if (gray[y * w + x] < threshold) 0 else 255
                out[y * w + x] = Color.rgb(value, value, value)
            }
        }
        val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        result.setPixels(out, 0, w, 0, 0, w, h)
        return result
    }

    private fun buildIntegralImage(gray: IntArray, w: Int, h: Int): LongArray {
        val integral = LongArray(w * h)
        for (y in 0 until h) {
            var rowSum = 0L
            for (x in 0 until w) {
                rowSum += gray[y * w + x]
                integral[y * w + x] = rowSum + if (y > 0) integral[(y - 1) * w + x] else 0L
            }
        }
        return integral
    }

    private fun regionSum(integral: LongArray, w: Int, x1: Int, y1: Int, x2: Int, y2: Int): Long {
        val a = if (x1 > 0 && y1 > 0) integral[y1 * w + (x1 - 1)] else 0L
        val b = if (y1 > 0) integral[y1 * w + x2] else 0L
        val c = if (x1 > 0) integral[y2 * w + (x1 - 1)] else 0L
        val d = integral[y2 * w + x2]
        return d - b - c + a
    }

    private fun sharpen(bitmap: Bitmap): Bitmap {
        val kernel = floatArrayOf(
            0f, -1f, 0f,
            -1f, 5f, -1f,
            0f, -1f, 0f
        )
        return convolve(bitmap, kernel)
    }

    private fun convolve(bitmap: Bitmap, kernel: FloatArray): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
        val out = IntArray(w * h)
        for (y in 0 until h) {
            for (x in 0 until w) {
                var sum = 0f
                var ki = 0
                for (ky in -1..1) {
                    for (kx in -1..1) {
                        val nx = (x + kx).coerceIn(0, w - 1)
                        val ny = (y + ky).coerceIn(0, h - 1)
                        sum += Color.red(pixels[ny * w + nx]) * kernel[ki++]
                    }
                }
                val v = sum.roundToInt().coerceIn(0, 255)
                out[y * w + x] = Color.rgb(v, v, v)
            }
        }
        val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        result.setPixels(out, 0, w, 0, 0, w, h)
        return result
    }

    private fun deskew(bitmap: Bitmap): Bitmap {
        var bestAngle = 0f
        var bestScore = Double.NEGATIVE_INFINITY
        val angles = listOf(-8f, -6f, -4f, -2f, 0f, 2f, 4f, 6f, 8f)
        for (angle in angles) {
            val score = projectionScore(bitmap, angle)
            if (score > bestScore) {
                bestScore = score
                bestAngle = angle
            }
        }
        if (abs(bestAngle) < 0.5f) return bitmap
        return rotateBitmap(bitmap, bestAngle)
    }

    private fun projectionScore(bitmap: Bitmap, angle: Float): Double {
        val rotated = rotateBitmap(bitmap, angle)
        val w = rotated.width
        val h = rotated.height
        val pixels = IntArray(w * h)
        rotated.getPixels(pixels, 0, w, 0, 0, w, h)
        rotated.recycle()
        val projection = DoubleArray(h)
        for (y in 0 until h) {
            var sum = 0.0
            for (x in 0 until w) {
                if (Color.red(pixels[y * w + x]) < 128) sum++
            }
            projection[y] = sum
        }
        var variance = 0.0
        val mean = projection.average()
        for (value in projection) {
            val d = value - mean
            variance += d * d
        }
        return variance
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val radians = Math.toRadians(degrees.toDouble())
        val cos = abs(Math.cos(radians))
        val sin = abs(Math.sin(radians))
        val newW = (bitmap.width * cos + bitmap.height * sin).toInt()
        val newH = (bitmap.width * sin + bitmap.height * cos).toInt()
        val out = Bitmap.createBitmap(newW, newH, Bitmap.Config.ARGB_8888)
        val canvas = AndroidCanvas(out)
        canvas.translate(newW / 2f, newH / 2f)
        canvas.rotate(degrees)
        canvas.translate(-bitmap.width / 2f, -bitmap.height / 2f)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        return out
    }

    companion object {
        const val BLUR_THRESHOLD = 85.0
    }
}
