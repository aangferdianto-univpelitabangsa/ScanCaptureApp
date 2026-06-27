package com.example.scancaptureapp.presentation.screens.camera

import android.content.Context
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.scancaptureapp.R
import com.example.scancaptureapp.presentation.viewmodel.LensFacing
import com.google.common.util.concurrent.ListenableFuture
import java.io.File
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private const val TAG = "CameraCaptureHelper"

/**
 * Result of binding CameraX use cases to a lifecycle.
 */
data class BoundCameraSession(
    val imageCapture: ImageCapture,
    val camera: Camera?
)

/**
 * Safely binds Preview + ImageCapture to [lifecycleOwner].
 *
 * Crash fix: previously [ImageCapture] was created in `remember {}` but bound asynchronously
 * inside [AndroidView.factory]; capture could run before bind finished → IllegalStateException.
 */
object CameraCaptureHelper {

    fun getCameraProviderFuture(context: Context): ListenableFuture<ProcessCameraProvider> =
        ProcessCameraProvider.getInstance(context)

    fun bindCameraUseCases(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        lensFacing: LensFacing,
        flashMode: Int,
        onBound: (BoundCameraSession) -> Unit,
        onError: (String) -> Unit
    ) {
        val mainExecutor = ContextCompat.getMainExecutor(context)
        val future = getCameraProviderFuture(context)

        future.addListener({
            try {
                val cameraProvider = future.get()
                cameraProvider.unbindAll()

                val preview = Preview.Builder()
                    .build()
                    .also { it.surfaceProvider = previewView.surfaceProvider }

                val resolutionSelector = ResolutionSelector.Builder()
                    .setResolutionStrategy(ResolutionStrategy.HIGHEST_AVAILABLE_STRATEGY)
                    .build()

                val imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .setResolutionSelector(resolutionSelector)
                    .setFlashMode(flashMode)
                    .build()

                // Match sensor orientation to avoid capture failures on some devices
                imageCapture.targetRotation = previewView.display?.rotation
                    ?: android.view.Surface.ROTATION_0

                val cameraSelector = lensFacing.toCameraSelector()
                if (!cameraProvider.hasCamera(cameraSelector)) {
                    onError(context.getString(R.string.error_camera_not_available))
                    return@addListener
                }

                val camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )

                onBound(BoundCameraSession(imageCapture, camera))
            } catch (e: Exception) {
                Log.e(TAG, "Camera bind failed", e)
                onError(e.message ?: context.getString(R.string.error_camera_start_failed))
            }
        }, mainExecutor)
    }

    fun unbindAll(context: Context) {
        try {
            val future = getCameraProviderFuture(context)
            if (future.isDone) {
                future.get().unbindAll()
            }
        } catch (e: Exception) {
            Log.w(TAG, "unbindAll failed", e)
        }
    }

    fun setTorch(camera: Camera?, enabled: Boolean) {
        try {
            camera?.cameraControl?.enableTorch(enabled)
        } catch (e: Exception) {
            Log.w(TAG, "Torch toggle failed", e)
        }
    }

    /**
     * Captures to [outputFile]. Callbacks are dispatched on [callbackExecutor] (use main executor for navigation).
     *
     * Crash fix: must only call when [imageCapture] is the same instance that was bound to the provider.
     */
    fun captureToFile(
        context: Context,
        imageCapture: ImageCapture,
        outputFile: File,
        cameraExecutor: ExecutorService,
        callbackExecutor: Executor,
        onSuccess: (File) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            outputFile.parentFile?.mkdirs()
            if (outputFile.parentFile?.exists() != true) {
                onError(context.getString(R.string.error_cannot_create_output_dir))
                return
            }

            val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

            imageCapture.takePicture(
                outputOptions,
                cameraExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        callbackExecutor.execute {
                            if (outputFile.exists() && outputFile.length() > 0L) {
                                onSuccess(outputFile)
                            } else {
                                onError(context.getString(R.string.error_capture_empty_file))
                            }
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Log.e(TAG, "takePicture failed: ${exception.imageCaptureError}", exception)
                        callbackExecutor.execute {
                            onError(
                                exception.message
                                    ?: context.getString(
                                        R.string.error_capture_failed_code,
                                        exception.imageCaptureError
                                    )
                            )
                        }
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "captureToFile threw", e)
            callbackExecutor.execute {
                onError(e.message ?: context.getString(R.string.error_capture_unexpected))
            }
        }
    }

    fun newCameraExecutor(): ExecutorService =
        Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable, "CameraX-IO").apply { isDaemon = true }
        }

    fun shutdownExecutor(executor: ExecutorService?) {
        try {
            executor?.shutdown()
        } catch (e: Exception) {
            Log.w(TAG, "Executor shutdown failed", e)
        }
    }
}
