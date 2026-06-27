package com.example.scancaptureapp.data.repository

import android.content.Context
import android.net.Uri
import com.example.scancaptureapp.R
import com.example.scancaptureapp.data.image.BitmapLoader
import com.example.scancaptureapp.data.image.ImagePreprocessor
import com.example.scancaptureapp.data.ocr.MlKitOcrEngine
import com.example.scancaptureapp.data.ocr.TextPostProcessor
import com.example.scancaptureapp.domain.model.OcrRequest
import com.example.scancaptureapp.domain.model.OcrResult
import com.example.scancaptureapp.domain.repository.ImageProcessor
import com.example.scancaptureapp.domain.repository.OcrRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OcrRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageProcessor: ImageProcessor,
    private val ocrEngine: MlKitOcrEngine,
    private val textPostProcessor: TextPostProcessor
) : OcrRepository {

    override suspend fun recognizeText(imageUri: Uri, request: OcrRequest): Result<OcrResult> =
        withContext(Dispatchers.Default) {
            runCatching {
                val bitmap = BitmapLoader.decodeFromUri(context, imageUri)
                recognizeFromBitmap(bitmap, request)
            }
        }

    override suspend fun recognizeTextFromPath(imagePath: String, request: OcrRequest): Result<OcrResult> =
        withContext(Dispatchers.Default) {
            runCatching {
                val bitmap = BitmapLoader.decodeFromPath(imagePath)
                recognizeFromBitmap(bitmap, request)
            }
        }

    private suspend fun recognizeFromBitmap(bitmap: android.graphics.Bitmap, request: OcrRequest): OcrResult {
        val blurScore = if (request.checkBlur) imageProcessor.detectBlurScore(bitmap) else 0.0
        val isBlurry = request.checkBlur && blurScore < ImagePreprocessor.BLUR_THRESHOLD

        val processed = imageProcessor.processForOcr(
            source = bitmap,
            scanMode = request.scanMode,
            enhance = request.enhanceImage
        )
        if (processed != bitmap) bitmap.recycle()

        try {
            val rawText = ocrEngine.recognize(processed).trim()
            if (rawText.isEmpty()) {
                throw IllegalStateException(context.getString(R.string.error_no_text_detected))
            }
            val cleaned = textPostProcessor.format(rawText, request.language)
            if (cleaned.isBlank()) {
                throw IllegalStateException(context.getString(R.string.error_no_text_detected_hint))
            }
            return OcrResult(text = cleaned, isBlurry = isBlurry)
        } finally {
            processed.recycle()
        }
    }
}
