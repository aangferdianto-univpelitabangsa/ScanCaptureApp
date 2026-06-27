package com.example.scancaptureapp.data.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ML Kit Text Recognition v2 (Latin) — supports Indonesian and English in Latin script.
 */
@Singleton
class MlKitOcrEngine @Inject constructor() {

    private val recognizer by lazy {
        TextRecognition.getClient(
            TextRecognizerOptions.Builder()
                .build()
        )
    }

    suspend fun recognize(bitmap: Bitmap): String {
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        val visionText = recognizer.process(inputImage).await()
        return visionText.text
    }
}
