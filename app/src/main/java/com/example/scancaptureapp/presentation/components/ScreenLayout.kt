package com.example.scancaptureapp.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Standard screen shell: gradient background + padded content column.
 */
@Composable
fun AppScreenLayout(
    modifier: Modifier = Modifier,
    scrollable: Boolean = true,
    animatedBackground: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    BackgroundGradient(animated = animatedBackground) {
        val columnModifier = modifier
            .fillMaxSize()
            .padding(AppSpacing.standard)
            .let { base ->
                if (scrollable) base.verticalScroll(rememberScrollState()) else base
            }

        Column(modifier = columnModifier, content = content)
    }
}

@Composable
fun AppScreenBox(
    modifier: Modifier = Modifier,
    animatedBackground: Boolean = true,
    content: @Composable () -> Unit
) {
    BackgroundGradient(animated = animatedBackground) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(AppSpacing.standard)
        ) {
            content()
        }
    }
}
