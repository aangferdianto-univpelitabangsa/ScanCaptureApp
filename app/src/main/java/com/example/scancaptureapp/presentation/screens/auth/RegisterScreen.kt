package com.example.scancaptureapp.presentation.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            viewModel.clearSuccess()
            onRegisterSuccess()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    BackgroundGradient {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.register),
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            if (uiState.isLoading) {
                LoadingOverlay(stringResource(R.string.creating_account))
            }

            AnimatedVisibility(
                visible = !uiState.isLoading,
                enter = fadeIn()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(AppSpacing.standard)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.section)
                ) {
                    ScreenHeader(
                        title = stringResource(R.string.create_account),
                        subtitle = stringResource(R.string.create_account_subtitle)
                    )

                    AppCard {
                        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.standard)) {
                            AppTextField(
                                value = uiState.name,
                                onValueChange = viewModel::onNameChange,
                                label = stringResource(R.string.name),
                                leadingIcon = Icons.Default.Person,
                                error = uiState.nameError
                            )
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
                            AppTextField(
                                value = uiState.confirmPassword,
                                onValueChange = viewModel::onConfirmPasswordChange,
                                label = stringResource(R.string.confirm_password),
                                leadingIcon = Icons.Default.Lock,
                                isPassword = true,
                                error = uiState.confirmPasswordError
                            )
                            AppPrimaryButton(
                                text = stringResource(R.string.register),
                                onClick = viewModel::register
                            )
                        }
                    }
                }
            }
        }
    }
}
