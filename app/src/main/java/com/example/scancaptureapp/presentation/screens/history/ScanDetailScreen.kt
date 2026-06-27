package com.example.scancaptureapp.presentation.screens.history

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.scancaptureapp.R
import com.example.scancaptureapp.presentation.components.AppCard
import com.example.scancaptureapp.presentation.components.AppShape
import com.example.scancaptureapp.presentation.components.AppSpacing
import com.example.scancaptureapp.presentation.components.BackgroundGradient
import com.example.scancaptureapp.presentation.components.glassSurfaceColor
import com.example.scancaptureapp.presentation.viewmodel.HistoryViewModel
import com.example.scancaptureapp.utils.copyToClipboard
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanDetailScreen(
    scanId: Long,
    onNavigateBack: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val scan by viewModel.selectedScan.collectAsState()
    val context = LocalContext.current
    val clipboardLabel = stringResource(R.string.clipboard_scan_detail)

    LaunchedEffect(scanId) {
        viewModel.loadScanDetail(scanId)
    }

    BackgroundGradient {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.scan_detail)) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { padding ->
            scan?.let { item ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(AppSpacing.screen)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(AppSpacing.section)
                ) {
                    AppCard {
                        AsyncImage(
                            model = File(item.imagePath),
                            contentDescription = stringResource(R.string.scan_image),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentScale = ContentScale.Fit
                        )
                    }

                    OutlinedTextField(
                        value = item.extractedText,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        shape = AppShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = glassSurfaceColor(),
                            unfocusedContainerColor = glassSurfaceColor(),
                            disabledContainerColor = glassSurfaceColor(),
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    FilledTonalButton(
                        onClick = { context.copyToClipboard(clipboardLabel, item.extractedText) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = AppShape
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null)
                        Text(
                            text = stringResource(R.string.copy_text),
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }
            } ?: Text(
                stringResource(R.string.loading),
                modifier = Modifier.padding(AppSpacing.screen),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
