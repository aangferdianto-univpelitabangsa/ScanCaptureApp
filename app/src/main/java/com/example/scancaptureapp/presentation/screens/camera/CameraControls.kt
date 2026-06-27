package com.example.scancaptureapp.presentation.screens.camera

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.scancaptureapp.R
import com.example.scancaptureapp.presentation.components.pressScale
import com.example.scancaptureapp.ui.theme.CameraControlSurface
import com.example.scancaptureapp.ui.theme.GradientDeepPink
import com.example.scancaptureapp.ui.theme.GradientLightOrange
import com.example.scancaptureapp.ui.theme.GradientSoftCoral

@Composable
fun CameraBottomControls(
    onGalleryClick: () -> Unit,
    onCaptureClick: () -> Unit,
    onFlashClick: () -> Unit,
    captureEnabled: Boolean,
    flashEnabled: Boolean,
    isTorchOn: Boolean,
    showFlash: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 28.dp, vertical = 32.dp)
    ) {
        CameraSideButton(
            onClick = onGalleryClick,
            icon = Icons.Default.PhotoLibrary,
            contentDescription = stringResource(R.string.gallery),
            modifier = Modifier.align(Alignment.CenterStart)
        )

        CaptureShutterButton(
            onClick = onCaptureClick,
            enabled = captureEnabled,
            modifier = Modifier.align(Alignment.Center)
        )

        if (showFlash) {
            CameraSideButton(
                onClick = onFlashClick,
                icon = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                contentDescription = stringResource(R.string.flash),
                enabled = flashEnabled,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}

@Composable
private fun CameraSideButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        onClick = onClick,
        modifier = modifier
            .size(52.dp)
            .pressScale(interactionSource)
            .shadow(10.dp, CircleShape),
        shape = CircleShape,
        color = CameraControlSurface,
        enabled = enabled,
        interactionSource = interactionSource
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
private fun CaptureShutterButton(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val gradient = Brush.linearGradient(
        colors = listOf(GradientDeepPink, GradientSoftCoral, GradientLightOrange)
    )

    Box(
        modifier = modifier.size(84.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.size(84.dp),
            shape = CircleShape,
            color = Color.Transparent,
            border = BorderStroke(4.dp, Color.White.copy(alpha = 0.92f))
        ) {}

        Box(
            modifier = Modifier
                .size(70.dp)
                .shadow(14.dp, CircleShape)
                .clip(CircleShape)
                .background(if (enabled) gradient else Brush.linearGradient(
                    listOf(
                        GradientDeepPink.copy(alpha = 0.5f),
                        GradientSoftCoral.copy(alpha = 0.5f)
                    )
                ))
                .pressScale(interactionSource)
                .clickable(
                    enabled = enabled,
                    role = Role.Button,
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PhotoCamera,
                contentDescription = stringResource(R.string.capture),
                tint = Color.White,
                modifier = Modifier.size(34.dp)
            )
        }
    }
}
