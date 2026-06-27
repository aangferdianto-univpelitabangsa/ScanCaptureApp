package com.example.scancaptureapp.data.local

import android.content.Context
import com.example.scancaptureapp.domain.model.OcrLanguage
import com.example.scancaptureapp.domain.model.ScanMode
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OcrPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isEnhanceEnabled(): Boolean = prefs.getBoolean(KEY_ENHANCE, true)

    fun setEnhanceEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ENHANCE, enabled).apply()
    }

    fun getOcrLanguage(): OcrLanguage {
        return when (prefs.getString(KEY_OCR_LANGUAGE, OcrLanguage.AUTO.name)) {
            OcrLanguage.INDONESIAN.name -> OcrLanguage.INDONESIAN
            OcrLanguage.ENGLISH.name -> OcrLanguage.ENGLISH
            else -> OcrLanguage.AUTO
        }
    }

    fun setOcrLanguage(language: OcrLanguage) {
        prefs.edit().putString(KEY_OCR_LANGUAGE, language.name).apply()
    }

    fun getScanMode(): ScanMode {
        return when (prefs.getString(KEY_SCAN_MODE, ScanMode.DOCUMENT.name)) {
            ScanMode.RECEIPT.name -> ScanMode.RECEIPT
            ScanMode.HANDWRITING.name -> ScanMode.HANDWRITING
            else -> ScanMode.DOCUMENT
        }
    }

    fun setScanMode(mode: ScanMode) {
        prefs.edit().putString(KEY_SCAN_MODE, mode.name).apply()
    }

    companion object {
        private const val PREFS_NAME = "ocr_settings"
        private const val KEY_ENHANCE = "enhance_scan"
        private const val KEY_OCR_LANGUAGE = "ocr_language"
        private const val KEY_SCAN_MODE = "scan_mode"
    }
}
