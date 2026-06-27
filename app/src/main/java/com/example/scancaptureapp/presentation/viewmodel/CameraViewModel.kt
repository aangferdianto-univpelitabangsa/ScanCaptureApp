package com.example.scancaptureapp.presentation.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.lifecycle.ViewModel
import com.example.scancaptureapp.R
import com.example.scancaptureapp.utils.ImageFileManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import javax.inject.Inject

enum class LensFacing {
    BACK,
    FRONT;

    fun toCameraSelector(): CameraSelector = when (this) {
        BACK -> CameraSelector.DEFAULT_BACK_CAMERA
        FRONT -> CameraSelector.DEFAULT_FRONT_CAMERA
    }
}

data class CameraUiState(
    val isCameraReady: Boolean = false,
    val isCapturing: Boolean = false,
    val isTorchOn: Boolean = false,
    val lensFacing: LensFacing = LensFacing.BACK,
    val errorMessage: String? = null,
    val capturedImagePath: String? = null,
    val capturedImageUri: Uri? = null
)

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val imageFileManager: ImageFileManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    fun onCameraReady() {
        _uiState.update { it.copy(isCameraReady = true, errorMessage = null) }
    }

    fun onCameraBindFailed(message: String) {
        _uiState.update {
            it.copy(isCameraReady = false, errorMessage = message)
        }
    }

    fun onCameraUnbound() {
        _uiState.update { it.copy(isCameraReady = false) }
    }

    fun startCapture() {
        _uiState.update { it.copy(isCapturing = true, errorMessage = null) }
    }

    fun createCaptureOutputFile(): File = imageFileManager.createCacheCaptureFile()

    fun onCaptureSuccess(file: File) {
        val uri = imageFileManager.getUriForFile(file)
        _uiState.update {
            it.copy(
                isCapturing = false,
                capturedImagePath = file.absolutePath,
                capturedImageUri = uri,
                errorMessage = null
            )
        }
    }

    fun onCaptureError(message: String) {
        _uiState.update {
            it.copy(isCapturing = false, errorMessage = message)
        }
    }

    fun toggleTorch() {
        _uiState.update { it.copy(isTorchOn = !it.isTorchOn) }
    }

    fun switchCamera() {
        _uiState.update {
            it.copy(
                lensFacing = if (it.lensFacing == LensFacing.BACK) LensFacing.FRONT else LensFacing.BACK,
                isTorchOn = false,
                isCameraReady = false
            )
        }
    }

    fun clearCaptureEvent() {
        _uiState.update { it.copy(capturedImagePath = null, capturedImageUri = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun importFromGallery(uri: Uri) {
        try {
            val file = imageFileManager.copyUriToCache(uri)
            onCaptureSuccess(file)
        } catch (e: Exception) {
            Log.e("CameraViewModel", "Gallery import failed", e)
            onCaptureError(
                e.message ?: context.getString(R.string.error_gallery_import_failed)
            )
        }
    }
}
