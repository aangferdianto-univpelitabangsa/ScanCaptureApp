package com.example.scancaptureapp.presentation.screens.camera

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.scancaptureapp.R
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.scancaptureapp.presentation.components.AppPrimaryButton
import com.example.scancaptureapp.presentation.components.AppSpacing
import com.example.scancaptureapp.presentation.components.BackgroundGradient
import com.example.scancaptureapp.presentation.components.LoadingOverlay
import com.example.scancaptureapp.presentation.viewmodel.CameraViewModel
import com.example.scancaptureapp.presentation.viewmodel.LensFacing
import com.example.scancaptureapp.ui.theme.CameraOverlay
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    onImageCaptured: (imagePath: String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: CameraViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val mainExecutor = remember(context) { ContextCompat.getMainExecutor(context) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val cameraExecutor = remember { CameraCaptureHelper.newCameraExecutor() }

    var boundSession by remember { mutableStateOf<BoundCameraSession?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let(viewModel::importFromGallery)
    }

    DisposableEffect(Unit) {
        onDispose { CameraCaptureHelper.shutdownExecutor(cameraExecutor) }
    }

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    LaunchedEffect(uiState.capturedImagePath) {
        uiState.capturedImagePath?.let { path ->
            viewModel.clearCaptureEvent()
            onImageCaptured(path)
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = Color.Black,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                cameraPermissionState.status.isGranted -> {
                    CameraPreviewHost(
                        lensFacing = uiState.lensFacing,
                        isTorchOn = uiState.isTorchOn && uiState.lensFacing == LensFacing.BACK,
                        onSessionBound = { session ->
                            boundSession = session
                            viewModel.onCameraReady()
                        },
                        onBindFailed = viewModel::onCameraBindFailed,
                        onUnbound = {
                            boundSession = null
                            viewModel.onCameraUnbound()
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    if (uiState.isCapturing) {
                        LoadingOverlay(stringResource(R.string.capturing))
                    }

                    if (!uiState.isCameraReady && uiState.errorMessage == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(CameraOverlay),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .statusBarsPadding()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CameraTopIconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back),
                                tint = Color.White
                            )
                        }
                        CameraTopIconButton(
                            onClick = viewModel::switchCamera,
                            enabled = uiState.isCameraReady && !uiState.isCapturing,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.Cameraswitch,
                                contentDescription = stringResource(R.string.switch_camera),
                                tint = Color.White
                            )
                        }
                    }

                    CameraBottomControls(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        onGalleryClick = {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onFlashClick = viewModel::toggleTorch,
                        flashEnabled = uiState.isCameraReady && !uiState.isCapturing,
                        isTorchOn = uiState.isTorchOn,
                        showFlash = uiState.lensFacing == LensFacing.BACK,
                        onCaptureClick = {
                            val session = boundSession
                            if (session == null || !uiState.isCameraReady) {
                                viewModel.onCaptureError(context.getString(R.string.error_camera_not_ready))
                                return@CameraBottomControls
                            }
                            if (uiState.isCapturing) return@CameraBottomControls

                            viewModel.startCapture()
                            val outputFile = viewModel.createCaptureOutputFile()

                            if (uiState.isTorchOn && uiState.lensFacing == LensFacing.BACK) {
                                session.imageCapture.flashMode = ImageCapture.FLASH_MODE_ON
                            } else {
                                session.imageCapture.flashMode = ImageCapture.FLASH_MODE_OFF
                            }

                            CameraCaptureHelper.captureToFile(
                                context = context,
                                imageCapture = session.imageCapture,
                                outputFile = outputFile,
                                cameraExecutor = cameraExecutor,
                                callbackExecutor = mainExecutor,
                                onSuccess = viewModel::onCaptureSuccess,
                                onError = viewModel::onCaptureError
                            )
                        },
                        captureEnabled = uiState.isCameraReady && !uiState.isCapturing
                    )
                }

                cameraPermissionState.status.shouldShowRationale -> {
                    PermissionPlaceholder(
                        message = stringResource(R.string.camera_permission_rationale),
                        onRequest = { cameraPermissionState.launchPermissionRequest() },
                        onBack = onNavigateBack
                    )
                }

                else -> {
                    PermissionPlaceholder(
                        message = stringResource(R.string.camera_permission_denied),
                        onRequest = { cameraPermissionState.launchPermissionRequest() },
                        onBack = onNavigateBack
                    )
                }
            }
        }
    }
}

@Composable
private fun CameraTopIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = CircleShape,
        color = com.example.scancaptureapp.ui.theme.CameraControlSurface
    ) {
        Box(
            modifier = Modifier.padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
private fun PermissionPlaceholder(
    message: String,
    onRequest: () -> Unit,
    onBack: () -> Unit
) {
    BackgroundGradient {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppSpacing.screen),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                AppPrimaryButton(
                    text = stringResource(R.string.grant_permission),
                    onClick = onRequest,
                    modifier = Modifier.padding(top = AppSpacing.section)
                )
                TextButton(onClick = onBack, modifier = Modifier.padding(top = AppSpacing.small)) {
                    Text(stringResource(R.string.go_back))
                }
            }
        }
    }
}
