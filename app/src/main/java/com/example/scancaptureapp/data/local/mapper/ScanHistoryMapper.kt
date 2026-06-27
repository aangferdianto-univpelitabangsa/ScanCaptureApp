package com.example.scancaptureapp.data.local.mapper

import com.example.scancaptureapp.data.local.entity.ScanHistoryEntity
import com.example.scancaptureapp.domain.model.ScanHistoryItem

fun ScanHistoryEntity.toDomain(): ScanHistoryItem = ScanHistoryItem(
    id = id,
    imagePath = imagePath,
    extractedText = extractedText,
    timestamp = timestamp
)

fun ScanHistoryItem.toEntity(): ScanHistoryEntity = ScanHistoryEntity(
    id = id,
    imagePath = imagePath,
    extractedText = extractedText,
    timestamp = timestamp
)
