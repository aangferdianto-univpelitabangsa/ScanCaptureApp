package com.example.scancaptureapp.presentation.screens.splash

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.scancaptureapp.R
import com.example.scancaptureapp.presentation.components.AppSpacing
import com.example.scancaptureapp.presentation.components.BackgroundGradient
import com.example.scancaptureapp.presentation.viewmodel.SplashViewModel
import com.example.scancaptureapp.ui.theme.GradientDeepPink
import com.example.scancaptureapp.ui.theme.GradientLightOrange
import com.example.scancaptureapp.ui.theme.GradientSoftCoral

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToMain: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var animateIn by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { animateIn = true }

    LaunchedEffect(uiState.isLoading, uiState.isLoggedIn) {
        if (!uiState.isLoading) {
            try {
                if (uiState.isLoggedIn) {
                    Log.d(TAG, "Navigating to Main")
                    onNavigateToMain()
                } else {
                    Log.d(TAG, "Navigating to Login")
                    onNavigateToLogin()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Navigation from splash failed — fallback to login", e)
                onNavigateToLogin()
            }
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (animateIn) 1f else 0.85f,
        animationSpec = tween(600),
        label = "splashScale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (animateIn) 1f else 0f,
        animationSpec = tween(800),
        label = "splashAlpha"
    )

    BackgroundGradient {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    this.alpha = alpha
                    scaleX = scale
                    scaleY = scale
                },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .shadow(12.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(GradientDeepPink, GradientSoftCoral, GradientLightOrange)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DocumentScanner,
                    contentDescription = stringResource(R.string.splash_logo_desc),
                    modifier = Modifier.size(56.dp),
                    tint = Color.White
                )
            }

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = AppSpacing.section)
            )
            Text(
                text = stringResource(R.string.splash_tagline),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = AppSpacing.small)
            )
        }
    }
}

private const val TAG = "SplashScreen"
