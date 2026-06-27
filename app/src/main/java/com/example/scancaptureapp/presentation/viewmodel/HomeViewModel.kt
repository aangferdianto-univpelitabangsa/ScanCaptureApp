package com.example.scancaptureapp.presentation.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scancaptureapp.R
import com.example.scancaptureapp.data.local.OcrPreferences
import com.example.scancaptureapp.domain.model.OcrLanguage
import com.example.scancaptureapp.domain.model.OcrRequest
import com.example.scancaptureapp.domain.model.ScanMode
import com.example.scancaptureapp.domain.usecase.RecognizeTextUseCase
import com.example.scancaptureapp.domain.usecase.SaveScanUseCase
import com.example.scancaptureapp.utils.PdfExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class HomeUiState(
    val imagePath: String? = null,
    val imageUri: Uri? = null,
    val extractedText: String = "",
    val isProcessing: Boolean = false,
    val isExportingPdf: Boolean = false,
    val awaitingOcr: Boolean = false,
    val enhanceScan: Boolean = true,
    val ocrLanguage: OcrLanguage = OcrLanguage.AUTO,
    val scanMode: ScanMode = ScanMode.DOCUMENT,
    val isBlurryWarning: Boolean = false,
    val errorMessage: String? = null,
    val saveMessage: String? = null,
    val exportMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val recognizeTextUseCase: RecognizeTextUseCase,
    private val saveScanUseCase: SaveScanUseCase,
    private val ocrPreferences: OcrPreferences,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            enhanceScan = ocrPreferences.isEnhanceEnabled(),
            ocrLanguage = ocrPreferences.getOcrLanguage(),
            scanMode = ocrPreferences.getScanMode()
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun setCapturedImage(path: String, uri: Uri, showPreview: Boolean = true) {
        _uiState.update {
            it.copy(
                imagePath = path,
                imageUri = uri,
                extractedText = "",
                awaitingOcr = showPreview,
                isBlurryWarning = false,
                errorMessage = null,
                saveMessage = null,
                exportMessage = null
            )
        }
    }

    fun setEnhanceScan(enabled: Boolean) {
        ocrPreferences.setEnhanceEnabled(enabled)
        _uiState.update { it.copy(enhanceScan = enabled) }
    }

    fun setOcrLanguage(language: OcrLanguage) {
        ocrPreferences.setOcrLanguage(language)
        _uiState.update { it.copy(ocrLanguage = language) }
    }

    fun setScanMode(mode: ScanMode) {
        ocrPreferences.setScanMode(mode)
        _uiState.update { it.copy(scanMode = mode) }
    }

    fun updateExtractedText(text: String) {
        _uiState.update { it.copy(extractedText = text) }
    }

    fun dismissBlurWarning() {
        _uiState.update { it.copy(isBlurryWarning = false) }
    }

    fun runOcr(uri: Uri? = _uiState.value.imageUri, path: String? = _uiState.value.imagePath) {
        val imageUri = uri ?: return
        val imagePath = path
        val state = _uiState.value
        val request = OcrRequest(
            enhanceImage = state.enhanceScan,
            language = state.ocrLanguage,
            scanMode = state.scanMode,
            checkBlur = true
        )

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isProcessing = true,
                    awaitingOcr = false,
                    errorMessage = null,
                    isBlurryWarning = false
                )
            }
            val result = if (imagePath != null) {
                recognizeTextUseCase.fromPath(imagePath, request)
            } else {
                recognizeTextUseCase(imageUri, request)
            }
            result
                .onSuccess { ocrResult ->
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            extractedText = ocrResult.text,
                            isBlurryWarning = ocrResult.isBlurry
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            awaitingOcr = true,
                            errorMessage = e.message ?: context.getString(R.string.error_ocr_failed)
                        )
                    }
                }
        }
    }

    fun saveToHistory() {
        val state = _uiState.value
        val path = state.imagePath ?: return
        if (state.extractedText.isBlank()) {
            _uiState.update { it.copy(errorMessage = context.getString(R.string.error_no_text_to_save)) }
            return
        }
        viewModelScope.launch {
            saveScanUseCase(path, state.extractedText.trim())
            _uiState.update { it.copy(saveMessage = context.getString(R.string.saved_to_history)) }
        }
    }

    fun exportToPdf() {
        val text = _uiState.value.extractedText
        if (text.isBlank()) {
            _uiState.update { it.copy(errorMessage = context.getString(R.string.error_no_text_to_export)) }
            return
        }
        if (_uiState.value.isExportingPdf) return

        viewModelScope.launch {
            _uiState.update { it.copy(isExportingPdf = true, errorMessage = null, exportMessage = null) }
            val result = withContext(Dispatchers.IO) {
                PdfExporter.generatePdf(
                    context = context,
                    text = text.trim(),
                    title = context.getString(R.string.pdf_export_title)
                )
            }
            result
                .onSuccess { exportResult ->
                    Log.d(TAG, "PDF export success: ${exportResult.displayName}")
                    _uiState.update {
                        it.copy(
                            isExportingPdf = false,
                            exportMessage = context.getString(
                                R.string.pdf_saved_success,
                                exportResult.displayName
                            )
                        )
                    }
                }
                .onFailure { e ->
                    Log.e(TAG, "PDF export failed", e)
                    val message = when (e) {
                        is SecurityException -> context.getString(R.string.error_pdf_permission_denied)
                        else -> e.message ?: context.getString(R.string.error_pdf_export_failed)
                    }
                    _uiState.update {
                        it.copy(isExportingPdf = false, errorMessage = message)
                    }
                }
        }
    }

    fun clearScan() {
        _uiState.value = HomeUiState(
            enhanceScan = ocrPreferences.isEnhanceEnabled(),
            ocrLanguage = ocrPreferences.getOcrLanguage(),
            scanMode = ocrPreferences.getScanMode()
        )
    }

    fun clearMessages() {
        _uiState.update { it.copy(saveMessage = null, exportMessage = null, errorMessage = null) }
    }

    companion object {
        private const val TAG = "HomeViewModel"
    }
}
