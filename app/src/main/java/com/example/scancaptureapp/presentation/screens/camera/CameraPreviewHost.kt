package com.example.scancaptureapp.presentation.screens.camera

import android.view.ViewGroup
import androidx.camera.core.ImageCapture
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.scancaptureapp.presentation.viewmodel.LensFacing

/**
 * Hosts [PreviewView] and keeps a stable reference to the bound [ImageCapture] use case.
 */
@Composable
fun CameraPreviewHost(
    lensFacing: LensFacing,
    isTorchOn: Boolean,
    onSessionBound: (BoundCameraSession) -> Unit,
    onBindFailed: (String) -> Unit,
    onUnbound: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember {
        PreviewView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            // COMPATIBLE avoids some SurfaceView crashes on older devices in Compose
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    var boundSession by remember { mutableStateOf<BoundCameraSession?>(null) }

    DisposableEffect(lifecycleOwner, lensFacing) {
        CameraCaptureHelper.bindCameraUseCases(
            context = context,
            lifecycleOwner = lifecycleOwner,
            previewView = previewView,
            lensFacing = lensFacing,
            flashMode = ImageCapture.FLASH_MODE_OFF,
            onBound = { session ->
                boundSession = session
                CameraCaptureHelper.setTorch(session.camera, isTorchOn)
                onSessionBound(session)
            },
            onError = onBindFailed
        )

        onDispose {
            boundSession = null
            CameraCaptureHelper.unbindAll(context)
            onUnbound()
        }
    }

    // Torch can change without full rebind
    DisposableEffect(isTorchOn, boundSession) {
        CameraCaptureHelper.setTorch(boundSession?.camera, isTorchOn)
        onDispose { }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}
