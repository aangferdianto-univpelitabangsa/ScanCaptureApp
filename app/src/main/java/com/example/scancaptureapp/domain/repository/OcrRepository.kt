package com.example.scancaptureapp.domain.repository

import android.net.Uri
import com.example.scancaptureapp.domain.model.OcrRequest
import com.example.scancaptureapp.domain.model.OcrResult

interface OcrRepository {
    suspend fun recognizeText(imageUri: Uri, request: OcrRequest = OcrRequest()): Result<OcrResult>
    suspend fun recognizeTextFromPath(imagePath: String, request: OcrRequest = OcrRequest()): Result<OcrResult>
}
