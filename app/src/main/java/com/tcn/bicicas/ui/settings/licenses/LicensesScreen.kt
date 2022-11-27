package com.tcn.bicicas.ui.settings.licenses

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.Colors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
                val contentPadding =
                    WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
                        .asPaddingValues()
                LibrariesContainer(
                    contentPadding = contentPadding,
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
                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top))
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