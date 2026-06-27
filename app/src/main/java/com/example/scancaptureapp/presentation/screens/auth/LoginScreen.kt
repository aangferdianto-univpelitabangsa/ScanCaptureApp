package com.example.scancaptureapp.presentation.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.scancaptureapp.R
import com.example.scancaptureapp.presentation.components.AppCard
import com.example.scancaptureapp.presentation.components.AppPrimaryButton
import com.example.scancaptureapp.presentation.components.AppSpacing
import com.example.scancaptureapp.presentation.components.AppTextField
import com.example.scancaptureapp.presentation.components.BackgroundGradient
import com.example.scancaptureapp.presentation.components.LoadingOverlay
import com.example.scancaptureapp.presentation.components.ScreenHeader
import com.example.scancaptureapp.presentation.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            viewModel.clearSuccess()
            onLoginSuccess()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    BackgroundGradient {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            if (uiState.isLoading) {
                LoadingOverlay(stringResource(R.string.signing_in))
            }

            AnimatedVisibility(
                visible = !uiState.isLoading,
                enter = fadeIn(),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(AppSpacing.standard)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ScreenHeader(
                        title = stringResource(R.string.welcome_back),
                        subtitle = stringResource(R.string.sign_in_subtitle)
                    )

                    AppCard(modifier = Modifier.padding(top = AppSpacing.section)) {
                        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.standard)) {
                            AppTextField(
                                value = uiState.email,
                                onValueChange = viewModel::onEmailChange,
                                label = stringResource(R.string.email),
                                leadingIcon = Icons.Default.Email,
                                error = uiState.emailError
                            )
                            AppTextField(
                                value = uiState.password,
                                onValueChange = viewModel::onPasswordChange,
                                label = stringResource(R.string.password),
                                leadingIcon = Icons.Default.Lock,
                                isPassword = true,
                                error = uiState.passwordError
                            )
                            AppPrimaryButton(
                                text = stringResource(R.string.login),
                                onClick = viewModel::login
                            )
                        }
                    }

                    TextButton(
                        onClick = onNavigateToRegister,
                        modifier = Modifier.padding(top = AppSpacing.section)
                    ) {
                        Text(
                            text = stringResource(R.string.no_account_register),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
