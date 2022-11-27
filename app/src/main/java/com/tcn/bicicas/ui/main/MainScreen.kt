package com.tcn.bicicas.ui.main

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Pin
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.tcn.bicicas.R
import com.tcn.bicicas.data.model.Settings
import com.tcn.bicicas.data.model.Station
import com.tcn.bicicas.ui.pin.PinScreen
import com.tcn.bicicas.ui.settings.SettingsScreen
import com.tcn.bicicas.ui.settings.SettingsViewModel
import com.tcn.bicicas.ui.stations.StationsViewModel
import com.tcn.bicicas.ui.stations.list.StationScreen
import com.tcn.bicicas.ui.stations.map.MapScreen
import com.tcn.bicicas.ui.stations.map.MapState
import com.tcn.bicicas.ui.theme.BarHeight
import com.tcn.bicicas.ui.theme.BarTonalElevation
import com.tcn.bicicas.ui.theme.Theme
import kotlinx.coroutines.flow.map
import org.koin.androidx.compose.getViewModel

data class Screen(
    val icon: ImageVector,
    val content: @Composable (PaddingValues) -> Unit
)

@Composable
fun MainScreen(mapState: MapState<Station>) {
    val settingsViewModel: SettingsViewModel = getViewModel()
    val settings by settingsViewModel.settingsState.collectAsState()

    val darkTheme = settings.theme == Settings.Theme.Dark
            || (settings.theme == Settings.Theme.System && isSystemInDarkTheme())
    val dynamicColor = settings.dynamicColorEnabled
    val isPortrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT
    var settingsOpened by rememberSaveable { mutableStateOf(false) }
    val navigationBarContrastEnforced = !isPortrait
            || settings.navigationType == Settings.NavigationType.Tabs
            || settingsOpened

    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(Color.Transparent, !darkTheme)
        systemUiController.setNavigationBarColor(
            Color.Transparent, !darkTheme, navigationBarContrastEnforced
        )
    }

    val initialScreen = rememberSaveable {
        when (settings.initialScreen) {
            Settings.InitialScreen.Last -> settings.lastScreen
            else -> settings.initialScreen.id
        }
    }

    Theme(darkTheme = darkTheme, dynamicColor = dynamicColor) {
        Surface {
            MainContent(
                navigationType = settings.navigationType,
                initialScreen = initialScreen,
                mapState = mapState,
                onSettingsClick = { settingsOpened = true },
                onNavigatedToScreen = settingsViewModel::onLastScreenChanged
            )
            AnimatedVisibility(
                visible = settingsOpened,
                enter = slideInVertically(tween()) { it },
                exit = slideOutVertically(tween()) { it },
            ) {
                SettingsScreen { settingsOpened = false }
            }
        }
    }
}

@Composable
private fun MainContent(
    navigationType: Settings.NavigationType,
    initialScreen: Int,
    mapState: MapState<Station>,
    onSettingsClick: () -> Unit,
    onNavigatedToScreen: (Int) -> Unit,
) {

    val stationsViewModel: StationsViewModel = getViewModel()
    val navigateFlow = remember(stationsViewModel) {
        stationsViewModel.state.map { state -> state.navigateTo != null }
    }

    val screens = remember {
        listOf(
            Screen(Icons.Rounded.Pin) { padding ->
                PinScreen(padding)
            },
            Screen(Icons.Rounded.List) { padding ->
                StationScreen(padding, stationsViewModel)
            },
            Screen(Icons.Rounded.Map) { padding ->
                MapScreen(padding, stationsViewModel, mapState)
            },
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ApplicationTopBar(onSettingsClick)
        val navigateToMap by navigateFlow.collectAsState(false)
        when (navigationType) {
            Settings.NavigationType.Tabs -> TabsScreen(
                screens = screens,
                initialScreen = initialScreen,
                navigateToMap = navigateToMap,
                onNavigatedToScreen = onNavigatedToScreen,
            )

            Settings.NavigationType.BottomBar -> BottomBarScreen(
                screens = screens,
                initialScreen = initialScreen,
                navigateToMap = navigateToMap,
                onNavigatedToScreen = onNavigatedToScreen,
            )
        }
    }
}

@Composable
private fun ApplicationTopBar(onSettingsClick: () -> Unit) {
    Surface(
        tonalElevation = BarTonalElevation,
        shadowElevation = BarTonalElevation
    ) {
        Box(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                .height(BarHeight)
                .padding(horizontal = 8.dp)
                .fillMaxWidth()
        ) {
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.Center)
                    .height(24.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
            )
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.align(Alignment.CenterEnd),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = stringResource(R.string.settings_title),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}