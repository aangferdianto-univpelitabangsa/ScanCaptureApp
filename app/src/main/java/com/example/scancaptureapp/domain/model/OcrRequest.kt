package com.example.scancaptureapp.domain.model

data class OcrRequest(
    val enhanceImage: Boolean = true,
    val language: OcrLanguage = OcrLanguage.AUTO,
    val scanMode: ScanMode = ScanMode.DOCUMENT,
    val checkBlur: Boolean = true
)

data class OcrResult(
    val text: String,
    val isBlurry: Boolean = false
)
