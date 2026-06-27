package com.example.scancaptureapp.presentation.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.scancaptureapp.R
import com.example.scancaptureapp.presentation.components.AppCard
import com.example.scancaptureapp.presentation.components.AppPrimaryButton
import com.example.scancaptureapp.presentation.components.AppScreenLayout
import com.example.scancaptureapp.presentation.components.AppSpacing
import com.example.scancaptureapp.presentation.components.LanguageToggle
import com.example.scancaptureapp.presentation.components.ScreenHeader
import com.example.scancaptureapp.presentation.viewmodel.ProfileViewModel
import com.example.scancaptureapp.ui.theme.GradientDeepPink
import com.example.scancaptureapp.ui.theme.GradientLightOrange
import com.example.scancaptureapp.ui.theme.GradientSoftCoral

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsState()
    val loggedOut by viewModel.loggedOut.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    var showAbout by remember { mutableStateOf(false) }

    LaunchedEffect(loggedOut) {
        if (loggedOut) onLogout()
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text(stringResource(R.string.about_title)) },
            text = { Text(stringResource(R.string.about_message)) },
            confirmButton = {
                TextButton(onClick = { showAbout = false }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }

    AppScreenLayout(scrollable = true) {
        ScreenHeader(
            title = stringResource(R.string.nav_profile),
            subtitle = stringResource(R.string.profile_subtitle)
        )

        AppCard {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.standard)
            ) {
                val displayName = user?.name ?: stringResource(R.string.user_default_name)
                val initial = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(GradientDeepPink, GradientSoftCoral, GradientLightOrange)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initial,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 36.sp
                        )
                    )
                }

                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = user?.email ?: stringResource(R.string.not_available),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        AppCard {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.standard)) {
                ListItem(
                    headlineContent = {
                        Text(
                            stringResource(R.string.language_title),
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    leadingContent = {
                        Icon(
                            Icons.Default.Language,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
                LanguageToggle(
                    currentLanguage = currentLanguage,
                    onLanguageSelected = viewModel::setLanguage
                )
            }
        }

        AppCard {
            ListItem(
                headlineContent = {
                    Text(
                        stringResource(R.string.about_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                supportingContent = {
                    Text(
                        stringResource(R.string.about_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingContent = {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            TextButton(
                onClick = { showAbout = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.view_details))
            }
        }

        AppPrimaryButton(
            text = stringResource(R.string.logout),
            onClick = viewModel::logout,
            leadingIcon = Icons.AutoMirrored.Filled.Logout
        )
    }
}
