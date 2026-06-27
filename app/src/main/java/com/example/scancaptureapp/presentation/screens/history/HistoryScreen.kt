package com.example.scancaptureapp.presentation.screens.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.scancaptureapp.R
import com.example.scancaptureapp.domain.model.ScanHistoryItem
import com.example.scancaptureapp.presentation.components.AppScreenLayout
import com.example.scancaptureapp.presentation.components.AppShapeLarge
import com.example.scancaptureapp.presentation.components.AppSpacing
import com.example.scancaptureapp.presentation.components.ScreenHeader
import com.example.scancaptureapp.presentation.components.glassSurfaceColor
import com.example.scancaptureapp.presentation.viewmodel.HistoryViewModel
import com.example.scancaptureapp.utils.copyToClipboard
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    onItemClick: (Long) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val items by viewModel.historyItems.collectAsState()
    val context = LocalContext.current
    val clipboardLabel = stringResource(R.string.clipboard_history_text)

    AppScreenLayout(scrollable = false) {
        ScreenHeader(
            title = stringResource(R.string.nav_history),
            subtitle = if (items.isEmpty()) {
                stringResource(R.string.history_empty_subtitle)
            } else {
                stringResource(R.string.history_count, items.size)
            }
        )

        if (items.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = AppSpacing.section),
                shape = AppShapeLarge,
                colors = CardDefaults.cardColors(containerColor = glassSurfaceColor()),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Text(
                    stringResource(R.string.history_empty_title),
                    modifier = Modifier.padding(AppSpacing.section),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = AppSpacing.standard),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(AppSpacing.standard)
            ) {
                items(items, key = { it.id }) { item ->
                    HistoryListItem(
                        item = item,
                        onClick = { onItemClick(item.id) },
                        onCopy = { context.copyToClipboard(clipboardLabel, item.extractedText) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryListItem(
    item: ScanHistoryItem,
    onClick: () -> Unit,
    onCopy: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = AppShapeLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = glassSurfaceColor())
    ) {
        Row(
            modifier = Modifier.padding(AppSpacing.standard),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = File(item.imagePath),
                contentDescription = stringResource(R.string.scan_preview),
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = AppSpacing.standard)
            ) {
                Text(
                    text = item.extractedText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = dateFormat.format(Date(item.timestamp)),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = AppSpacing.small)
                )
            }
            IconButton(
                onClick = onCopy,
                modifier = Modifier.size(48.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = stringResource(R.string.copy),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
