package com.example.scancaptureapp.domain.model

/**
 * A saved scan result shown in history and detail screens.
 */
data class ScanHistoryItem(
    val id: Long = 0,
    val imagePath: String,
    val extractedText: String,
    val timestamp: Long
)
