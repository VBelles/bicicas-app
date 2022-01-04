package com.tcn.bicicas.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ScrollableAlertDialog(
    onDismissRequest: () -> Unit,
    title: @Composable () -> Unit = {},
    text: @Composable () -> Unit = {},
    confirmButton: @Composable () -> Unit = {},
    dismissButton: @Composable () -> Unit = {},
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = RoundedCornerShape(28.0.dp),
            tonalElevation = 0.dp,
            modifier = Modifier
                .widthIn(max = 500.dp)
                .fillMaxWidth(0.85f)
                .padding(bottom = 24.dp)
        ) {
            Column(Modifier.padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 0.dp)) {
                ProvideTextStyle(MaterialTheme.typography.headlineSmall) {
                    title()
                }
                Spacer(modifier = Modifier.height(16.dp))
                val scrollState = rememberScrollState()
                Box(modifier = Modifier
                    .weight(1f, fill = false)
                    .graphicsLayer { alpha = 0.99F }
                    .drawWithContent {
                        drawContent()
                        if (scrollState.value != scrollState.maxValue) {
                            drawRect(
                                brush = Brush.verticalGradient(
                                    0.8f to Color.Black,
                                    1f to Color.Transparent
                                ),
                                blendMode = BlendMode.DstIn
                            )
                        }
                    }) {

                    Box(Modifier.verticalScroll(scrollState)) {
                        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
                            ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                                text()
                            }
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    dismissButton()
                    confirmButton()
                }
            }
        }
    }
}