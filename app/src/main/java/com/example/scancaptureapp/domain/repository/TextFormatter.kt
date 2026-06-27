package com.example.scancaptureapp.domain.repository

import com.example.scancaptureapp.domain.model.OcrLanguage

interface TextFormatter {
    fun format(rawText: String, language: OcrLanguage): String
}
