package com.tcn.bicicas.ui.main

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Pin
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tcn.bicicas.R
import com.tcn.bicicas.data.model.Settings
import com.tcn.bicicas.data.model.Station
import com.tcn.bicicas.ui.pin.PinScreen
import com.tcn.bicicas.ui.settings.SettingsScreen
import com.tcn.bicicas.ui.settings.SettingsViewModel
import com.tcn.bicicas.ui.settings.licenses.LicensesScreen
import com.tcn.bicicas.ui.stations.StationsViewModel
import com.tcn.bicicas.ui.stations.list.StationScreen
import com.tcn.bicicas.ui.stations.map.MapScreen
import com.tcn.bicicas.ui.stations.map.MapState
import com.tcn.bicicas.ui.theme.Theme
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import org.koin.androidx.compose.getViewModel

@Composable
fun MainScreen(
    mapState: MapState<Station>,
    hideSplashScreen: () -> Unit,
    updateSystemUiTheme: (darkMode: Boolean, contrastEnforced: Boolean) -> Unit
) {
    val settingsViewModel: SettingsViewModel = getViewModel()
    val settings by settingsViewModel.settingsState.collectAsState()

    val darkTheme = settings.theme == Settings.Theme.Dark
            || (settings.theme == Settings.Theme.System && isSystemInDarkTheme())
    val dynamicColor = settings.dynamicColorEnabled
    val isPortrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT
    val navigationBarContrastEnforced = !isPortrait
            || settings.navigationType == Settings.NavigationType.Tabs

    LaunchedEffect(darkTheme, navigationBarContrastEnforced) {
        updateSystemUiTheme(darkTheme, navigationBarContrastEnforced)
    }

    val initialScreen = rememberSaveable {
        when (settings.initialScreen) {
            Settings.InitialScreen.Last -> settings.lastScreen
            else -> settings.initialScreen.id
        }
    }

    val navigationState = remember { NavigationState(Screen.entries[initialScreen]) }

    LaunchedEffect(navigationState.screen) {
        settingsViewModel.onLastScreenChanged(navigationState.screen.ordinal)
    }

    val navController = rememberNavController()
    Theme(darkTheme = darkTheme, dynamicColor = dynamicColor) {
        Surface {
            NavHost(navController, startDestination = "main") {
                composable("main") {
                    MainContent(
                        navigationType = settings.navigationType,
                        navigationState = navigationState,
                        mapState = mapState,
                        onSettingsClick = { navController.navigate("settings") },
                        hideSplashScreen = hideSplashScreen,
                    )
                }

                composable("settings") {
                    SettingsScreen(
                        onBackClicked = { navController.popBackStack() },
                        onNavigateToLicenses = { navController.navigate("licenses") }
                    )
                }
                composable("licenses") {
                    LicensesScreen(
                        onBackClicked = { navController.popBackStack() },
                    )
                }
            }

        }
    }
}

private val iconForScreen = { screen: Screen ->
    when (screen) {
        Screen.Pin -> Icons.Rounded.Pin
        Screen.List -> Icons.Rounded.List
        Screen.Map -> Icons.Rounded.Map
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainContent(
    navigationType: Settings.NavigationType,
    navigationState: NavigationState,
    mapState: MapState<Station>,
    onSettingsClick: () -> Unit,
    hideSplashScreen: () -> Unit,
) {

    val stationsViewModel: StationsViewModel = getViewModel()
    LaunchedEffect(Unit) {
        stationsViewModel.state.filter { state -> state.navigateTo != null }
            .distinctUntilChanged()
            .collect { navigationState.navigateTo(Screen.Map) }
    }

    LaunchedEffect(Unit) {
        if (navigationState.screen != Screen.List ||
            stationsViewModel.state.firstOrNull { it.hasLoaded } != null
        ) {
            hideSplashScreen()
        }
    }

    val contentForScreen = @Composable { screen: Screen, padding: PaddingValues ->
        when (screen) {
            Screen.Pin -> PinScreen(padding)
            Screen.List -> StationScreen(padding, stationsViewModel)
            Screen.Map -> MapScreen(padding, stationsViewModel, mapState)
        }
    }

    val scrollBehavior = when (navigationState.screen) {
        Screen.Pin, Screen.Map -> TopAppBarDefaults.pinnedScrollBehavior()
        Screen.List -> TopAppBarDefaults.enterAlwaysScrollBehavior()
    }

    when (navigationType) {
        Settings.NavigationType.Tabs -> TabsScaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = { ApplicationTopBar(scrollBehavior, onSettingsClick) },
            navigationState = navigationState,
            content = contentForScreen,
            iconForScreen = iconForScreen,
        )

        Settings.NavigationType.BottomBar -> BottomBarScaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = { ApplicationTopBar(scrollBehavior, onSettingsClick) },
            navigationState = navigationState,
            content = contentForScreen,
            iconForScreen = iconForScreen,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationTopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    onSettingsClick: () -> Unit
) {
    Column {
        CenterAlignedTopAppBar(
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                scrolledContainerColor = MaterialTheme.colorScheme.surface
            ),
            scrollBehavior = scrollBehavior,
            title = {
                Image(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier.height(24.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                )
            },
            actions = {
                IconButton(
                    onClick = onSettingsClick,
                    content = {
                        Icon(Icons.Rounded.Settings, stringResource(R.string.settings_title))
                    })
            }
        )
    }
}
