package com.example.scancaptureapp.presentation.screens.crop

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.scancaptureapp.R
import com.example.scancaptureapp.presentation.components.AppPrimaryButton
import com.example.scancaptureapp.presentation.components.AppSpacing
import com.example.scancaptureapp.presentation.components.BackgroundGradient
import com.example.scancaptureapp.utils.ImageFileManager
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropScreen(
    imagePath: String,
    imageFileManager: ImageFileManager,
    onCropComplete: (croppedPath: String) -> Unit,
    onNavigateBack: () -> Unit
) {
    var left by remember { mutableFloatStateOf(0.1f) }
    var top by remember { mutableFloatStateOf(0.1f) }
    var right by remember { mutableFloatStateOf(0.9f) }
    var bottom by remember { mutableFloatStateOf(0.9f) }

    BackgroundGradient {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.crop_document)) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            val cropped = imageFileManager.cropImage(imagePath, left, top, right, bottom)
                            onCropComplete(cropped.absolutePath)
                        }) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = stringResource(R.string.apply_crop)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            val x = (change.position.x / size.width).coerceIn(0f, 1f)
                            val y = (change.position.y / size.height).coerceIn(0f, 1f)
                            right = x.coerceAtLeast(left + 0.1f)
                            bottom = y.coerceAtLeast(top + 0.1f)
                        }
                    }
            ) {
                AsyncImage(
                    model = File(imagePath),
                    contentDescription = stringResource(R.string.image_to_crop),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cropRect = Rect(
                        offset = Offset(left * size.width, top * size.height),
                        size = Size((right - left) * size.width, (bottom - top) * size.height)
                    )
                    drawRect(
                        color = Color.Black.copy(alpha = 0.45f),
                        size = size
                    )
                    val clearPath = Path().apply {
                        addRect(cropRect)
                    }
                    drawPath(clearPath, Color.Transparent)
                    drawRect(
                        color = Color.White,
                        topLeft = cropRect.topLeft,
                        size = cropRect.size,
                        style = Stroke(width = 3f)
                    )
                }
            }

            Text(
                text = stringResource(R.string.crop_hint),
                modifier = Modifier.padding(AppSpacing.screen),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            AppPrimaryButton(
                text = stringResource(R.string.apply_and_scan),
                onClick = {
                    val cropped = imageFileManager.cropImage(imagePath, left, top, right, bottom)
                    onCropComplete(cropped.absolutePath)
                },
                modifier = Modifier.padding(AppSpacing.screen)
            )
        }
        }
    }
}
