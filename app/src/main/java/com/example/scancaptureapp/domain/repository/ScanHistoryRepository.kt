package com.example.scancaptureapp.domain.repository

import com.example.scancaptureapp.domain.model.ScanHistoryItem
import kotlinx.coroutines.flow.Flow

interface ScanHistoryRepository {
    fun getAllScans(): Flow<List<ScanHistoryItem>>
    suspend fun getScanById(id: Long): ScanHistoryItem?
    suspend fun saveScan(imagePath: String, extractedText: String): Long
    suspend fun deleteScan(id: Long)
}
