package com.example.scancaptureapp.domain.usecase

import android.net.Uri
import com.example.scancaptureapp.domain.model.OcrRequest
import com.example.scancaptureapp.domain.model.OcrResult
import com.example.scancaptureapp.domain.model.ScanHistoryItem
import com.example.scancaptureapp.domain.repository.OcrRepository
import com.example.scancaptureapp.domain.repository.ScanHistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RecognizeTextUseCase @Inject constructor(
    private val ocrRepository: OcrRepository
) {
    suspend operator fun invoke(imageUri: Uri, request: OcrRequest = OcrRequest()): Result<OcrResult> =
        ocrRepository.recognizeText(imageUri, request)

    suspend fun fromPath(imagePath: String, request: OcrRequest = OcrRequest()): Result<OcrResult> =
        ocrRepository.recognizeTextFromPath(imagePath, request)
}

class SaveScanUseCase @Inject constructor(
    private val scanHistoryRepository: ScanHistoryRepository
) {
    suspend operator fun invoke(imagePath: String, extractedText: String): Long =
        scanHistoryRepository.saveScan(imagePath, extractedText)
}

class GetScanHistoryUseCase @Inject constructor(
    private val scanHistoryRepository: ScanHistoryRepository
) {
    operator fun invoke(): Flow<List<ScanHistoryItem>> = scanHistoryRepository.getAllScans()
}

class GetScanByIdUseCase @Inject constructor(
    private val scanHistoryRepository: ScanHistoryRepository
) {
    suspend operator fun invoke(id: Long): ScanHistoryItem? =
        scanHistoryRepository.getScanById(id)
}
