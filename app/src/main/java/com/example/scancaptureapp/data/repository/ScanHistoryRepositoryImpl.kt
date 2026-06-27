package com.example.scancaptureapp.data.repository

import com.example.scancaptureapp.data.local.dao.ScanHistoryDao
import com.example.scancaptureapp.data.local.entity.ScanHistoryEntity
import com.example.scancaptureapp.data.local.mapper.toDomain
import com.example.scancaptureapp.domain.model.ScanHistoryItem
import com.example.scancaptureapp.domain.repository.ScanHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ScanHistoryRepositoryImpl @Inject constructor(
    private val scanHistoryDao: ScanHistoryDao
) : ScanHistoryRepository {

    override fun getAllScans(): Flow<List<ScanHistoryItem>> =
        scanHistoryDao.getAllScans().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getScanById(id: Long): ScanHistoryItem? =
        scanHistoryDao.getScanById(id)?.toDomain()

    override suspend fun saveScan(imagePath: String, extractedText: String): Long =
        scanHistoryDao.insertScan(
            ScanHistoryEntity(
                imagePath = imagePath,
                extractedText = extractedText
            )
        )

    override suspend fun deleteScan(id: Long) {
        scanHistoryDao.deleteScan(id)
    }
}
