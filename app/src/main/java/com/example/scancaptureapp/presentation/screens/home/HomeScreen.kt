package com.example.scancaptureapp.presentation.screens.home

import android.Manifest
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.scancaptureapp.R
import com.example.scancaptureapp.domain.model.OcrLanguage
import com.example.scancaptureapp.domain.model.ScanMode
import com.example.scancaptureapp.presentation.components.AppCard
import com.example.scancaptureapp.presentation.components.AppPrimaryButton
import com.example.scancaptureapp.presentation.components.AppSecondaryButton
import com.example.scancaptureapp.presentation.components.AppShape
import com.example.scancaptureapp.presentation.components.AppSpacing
import com.example.scancaptureapp.presentation.components.BackgroundGradient
import com.example.scancaptureapp.presentation.components.LoadingOverlay
import com.example.scancaptureapp.presentation.components.ScreenHeader
import com.example.scancaptureapp.presentation.components.glassSurfaceColor
import com.example.scancaptureapp.presentation.viewmodel.HomeViewModel
import com.example.scancaptureapp.utils.copyToClipboard
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File

@OptIn(ExperimentalPermissionsApi::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    onOpenCamera: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val activity = LocalContext.current as ComponentActivity
    val viewModel: HomeViewModel = hiltViewModel(activity)
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboardLabel = stringResource(R.string.clipboard_scan_text)

    val storagePermissionState = rememberPermissionState(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    val needsLegacyStoragePermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q

    LaunchedEffect(uiState.saveMessage, uiState.exportMessage, uiState.errorMessage) {
        uiState.saveMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
        uiState.exportMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
    }

    if (uiState.isBlurryWarning) {
        AlertDialog(
            onDismissRequest = viewModel::dismissBlurWarning,
            icon = { Icon(Icons.Default.Warning, contentDescription = null) },
            title = { Text(stringResource(R.string.blur_warning_title)) },
            text = { Text(stringResource(R.string.blur_warning_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.dismissBlurWarning()
                    onOpenCamera()
                }) {
                    Text(stringResource(R.string.retake_photo))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissBlurWarning) {
                    Text(stringResource(R.string.continue_anyway))
                }
            }
        )
    }

    BackgroundGradient {
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isProcessing -> {
                    LoadingOverlay(stringResource(R.string.extracting_text))
                }
                uiState.isExportingPdf -> {
                    LoadingOverlay(stringResource(R.string.generating_pdf))
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(AppSpacing.standard)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.section)
            ) {
            ScreenHeader(
                title = stringResource(R.string.app_name),
                subtitle = stringResource(R.string.home_subtitle)
            )

            if (uiState.imagePath == null && uiState.extractedText.isEmpty()) {
                AppCard {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.section)
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.height(56.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        AppPrimaryButton(
                            text = stringResource(R.string.open_camera),
                            onClick = onOpenCamera,
                            leadingIcon = Icons.Default.CameraAlt
                        )
                    }
                }
            }

            if (uiState.imagePath != null) {
                ScanPreviewCard(imagePath = uiState.imagePath!!)

                OcrSettingsCard(
                    enhanceScan = uiState.enhanceScan,
                    ocrLanguage = uiState.ocrLanguage,
                    scanMode = uiState.scanMode,
                    onEnhanceChange = viewModel::setEnhanceScan,
                    onLanguageChange = viewModel::setOcrLanguage,
                    onScanModeChange = viewModel::setScanMode
                )

                if (uiState.awaitingOcr) {
                    AppPrimaryButton(
                        text = stringResource(R.string.extract_text),
                        onClick = { viewModel.runOcr() },
                        leadingIcon = Icons.Default.Refresh
                    )
                    AppSecondaryButton(
                        text = stringResource(R.string.retake_photo),
                        onClick = {
                            viewModel.clearScan()
                            onOpenCamera()
                        }
                    )
                } else if (uiState.extractedText.isNotEmpty()) {
                    FilledTonalButton(
                        onClick = { viewModel.runOcr() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isProcessing,
                        shape = AppShape
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Text(
                            stringResource(R.string.reprocess_ocr),
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                    AppSecondaryButton(
                        text = stringResource(R.string.retake_photo),
                        onClick = {
                            viewModel.clearScan()
                            onOpenCamera()
                        }
                    )
                }
            }

            if (uiState.extractedText.isNotEmpty()) {
                AppCard {
                    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.standard)) {
                        Text(
                            stringResource(R.string.extracted_text),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            stringResource(R.string.edit_text_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = uiState.extractedText,
                            onValueChange = viewModel::updateExtractedText,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            shape = AppShape,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = glassSurfaceColor(),
                                unfocusedContainerColor = glassSurfaceColor(),
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                }

                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.item)
                ) {
                    FilledTonalButton(
                        onClick = { context.copyToClipboard(clipboardLabel, uiState.extractedText) },
                        modifier = Modifier.weight(1f),
                        enabled = uiState.extractedText.isNotBlank() && !uiState.isExportingPdf,
                        shape = AppShape
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null)
                        Text(stringResource(R.string.copy), modifier = Modifier.padding(start = 6.dp))
                    }
                    FilledTonalButton(
                        onClick = viewModel::saveToHistory,
                        modifier = Modifier.weight(1f),
                        enabled = uiState.extractedText.isNotBlank() && !uiState.isExportingPdf,
                        shape = AppShape
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Text(stringResource(R.string.save), modifier = Modifier.padding(start = 6.dp))
                    }
                }

                FilledTonalButton(
                    onClick = {
                        if (needsLegacyStoragePermission && !storagePermissionState.status.isGranted) {
                            storagePermissionState.launchPermissionRequest()
                        } else {
                            viewModel.exportToPdf()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.extractedText.isNotBlank() &&
                        !uiState.isExportingPdf &&
                        !uiState.isProcessing,
                    shape = AppShape
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                    Text(
                        text = stringResource(R.string.export_pdf),
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }
        }
        }
    }
}

@Composable
private fun ScanPreviewCard(imagePath: String) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
            Text(
                stringResource(R.string.scan_preview_title),
                style = MaterialTheme.typography.titleMedium
            )
            AsyncImage(
                model = File(imagePath),
                contentDescription = stringResource(R.string.scan_preview),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OcrSettingsCard(
    enhanceScan: Boolean,
    ocrLanguage: OcrLanguage,
    scanMode: ScanMode,
    onEnhanceChange: (Boolean) -> Unit,
    onLanguageChange: (OcrLanguage) -> Unit,
    onScanModeChange: (ScanMode) -> Unit
) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.section)) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.enhance_scan),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        stringResource(R.string.enhance_scan_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = enhanceScan, onCheckedChange = onEnhanceChange)
            }

            Text(stringResource(R.string.ocr_language), style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
                OcrLanguage.entries.forEach { lang ->
                    FilterChip(
                        selected = ocrLanguage == lang,
                        onClick = { onLanguageChange(lang) },
                        label = {
                            Text(
                                when (lang) {
                                    OcrLanguage.AUTO -> stringResource(R.string.ocr_lang_auto)
                                    OcrLanguage.INDONESIAN -> stringResource(R.string.ocr_lang_indonesian)
                                    OcrLanguage.ENGLISH -> stringResource(R.string.ocr_lang_english)
                                }
                            )
                        }
                    )
                }
            }

            Text(stringResource(R.string.scan_mode), style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
                ScanMode.entries.forEach { mode ->
                    FilterChip(
                        selected = scanMode == mode,
                        onClick = { onScanModeChange(mode) },
                        label = {
                            Text(
                                when (mode) {
                                    ScanMode.DOCUMENT -> stringResource(R.string.scan_mode_document)
                                    ScanMode.RECEIPT -> stringResource(R.string.scan_mode_receipt)
                                    ScanMode.HANDWRITING -> stringResource(R.string.scan_mode_handwriting)
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}
