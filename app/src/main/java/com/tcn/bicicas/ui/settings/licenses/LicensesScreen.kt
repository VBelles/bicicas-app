package com.tcn.bicicas.ui.settings.licenses

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.Colors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.systemBarsPadding
import com.mikepenz.aboutlibraries.ui.compose.LibrariesContainer
import com.tcn.bicicas.R
import com.tcn.bicicas.ui.theme.BarHeight
import com.tcn.bicicas.ui.theme.BarTonalElevation
import com.tcn.bicicas.ui.theme.LocalDarkTheme

@Composable
fun LicensesScreen(onBackClicked: () -> Unit) {
    BackHandler(onBack = onBackClicked)
    Surface {
        Column(modifier = Modifier.fillMaxSize()) {
            LicensesTopAppBar(onBackClicked)

            // Material3 to Material2 "bridge" for LibrariesContainer theming support
            androidx.compose.material.MaterialTheme(
                colors = Colors(
                    primary = MaterialTheme.colorScheme.primary,
                    primaryVariant = MaterialTheme.colorScheme.primary,
                    secondary = MaterialTheme.colorScheme.secondary,
                    secondaryVariant = MaterialTheme.colorScheme.secondary,
                    background = MaterialTheme.colorScheme.background,
                    surface = MaterialTheme.colorScheme.surface,
                    error = MaterialTheme.colorScheme.error,
                    onPrimary = MaterialTheme.colorScheme.onPrimary,
                    onSecondary = MaterialTheme.colorScheme.onSecondary,
                    onBackground = MaterialTheme.colorScheme.onBackground,
                    onSurface = MaterialTheme.colorScheme.onSurface,
                    onError = MaterialTheme.colorScheme.onError,
                    isLight = !LocalDarkTheme.current
                )
            ) {
                LibrariesContainer(
                    contentPadding = rememberInsetsPaddingValues(
                        insets = LocalWindowInsets.current.systemBars,
                        applyTop = false
                    ),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun LicensesTopAppBar(onBackClicked: () -> Unit) {
    Surface(tonalElevation = BarTonalElevation) {
        Box(
            modifier = Modifier
                .systemBarsPadding(bottom = false)
                .height(BarHeight)
                .padding(horizontal = 8.dp)
                .fillMaxWidth()
        ) {
            IconButton(
                onClick = onBackClicked,
                modifier = Modifier.align(Alignment.CenterStart),
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = stringResource(R.string.settings_item_licenses),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

    }
}