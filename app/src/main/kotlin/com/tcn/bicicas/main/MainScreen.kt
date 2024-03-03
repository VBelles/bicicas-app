package com.tcn.bicicas.main

import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tcn.bicicas.R
import com.tcn.bicicas.main.theme.Theme
import com.tcn.bicicas.pin.PinModule
import com.tcn.bicicas.pin.presentation.ui.PinScreen
import com.tcn.bicicas.settings.SettingsModule
import com.tcn.bicicas.settings.domain.Settings
import com.tcn.bicicas.settings.presentation.LicensesScreen
import com.tcn.bicicas.settings.presentation.SettingsScreen
import com.tcn.bicicas.stations.StationsModule
import com.tcn.bicicas.stations.domain.model.Station
import com.tcn.bicicas.stations.presentation.list.StationScreen
import com.tcn.bicicas.stations.presentation.map.MapScreen
import com.tcn.bicicas.stations.presentation.map.MapState

@Composable
fun MainScreen(
    mainModule: MainModule,
    settingsModule: SettingsModule,
    pinModule: PinModule,
    stationsModule: StationsModule,
    mapState: MapState<Station>,
    hideSplashScreen: () -> Unit,
) {

    val viewModel = viewModel { mainModule.mainViewModel }
    val state by viewModel.state.collectAsState()
    val settings = state.settings

    SideEffect {
        if (state.isInitialDataLoaded) hideSplashScreen()
    }

    if (settings == null || !state.isInitialDataLoaded) return

    val darkTheme = settings.theme == Settings.Theme.Dark
            || (settings.theme == Settings.Theme.System && isSystemInDarkTheme())

    val systemUiController = rememberSystemUiController()

    val initialScreen = rememberSaveable {
        when (settings.initialScreen) {
            Settings.InitialScreen.Last -> settings.lastScreen
            else -> settings.initialScreen.id
        }
    }

    val navigationState = remember { NavigationState(Screen.entries[initialScreen]) }

    LaunchedEffect(navigationState.screen) {
        viewModel.onLastScreenChanged(navigationState.screen.ordinal)
    }

    val navController = rememberNavController()
    Theme(darkTheme = darkTheme, dynamicColor = settings.dynamicColorEnabled) {
        val navigationBarColor = MaterialTheme.colorScheme.background
        LaunchedEffect(darkTheme, navigationBarColor) {
            systemUiController.updateSystemUiTheme(darkTheme, navigationBarColor)
        }
        Surface {
            NavHost(navController, startDestination = "main") {
                composable("main") {
                    MainContent(
                        stationsModule = stationsModule,
                        pinModule = pinModule,
                        navigationType = settings.navigationType,
                        navigationState = navigationState,
                        mapState = mapState,
                        onSettingsClick = { navController.navigate("settings") },
                    )
                }

                composable("settings") {
                    SettingsScreen(
                        module = settingsModule,
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
        Screen.List -> Icons.AutoMirrored.Rounded.List
        Screen.Map -> Icons.Rounded.Map
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainContent(
    stationsModule: StationsModule,
    pinModule: PinModule,
    navigationType: Settings.NavigationType,
    navigationState: NavigationState,
    mapState: MapState<Station>,
    onSettingsClick: () -> Unit,
) {

    val contentForScreen = @Composable { screen: Screen, padding: PaddingValues ->
        when (screen) {
            Screen.Pin -> PinScreen(pinModule, padding)
            Screen.List -> StationScreen(padding, stationsModule) { navigationState.navigateTo(Screen.Map) }
            Screen.Map -> MapScreen(padding, stationsModule, mapState)
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
                content = { Icon(Icons.Rounded.Settings, stringResource(R.string.settings_title)) })
        }
    )
}


class SystemUiController(
    private val window: Window,
    private val insetsController: WindowInsetsControllerCompat,
) {

    fun updateSystemUiTheme(
        darkTheme: Boolean,
        navigationBarColor: androidx.compose.ui.graphics.Color
    ) {
        insetsController.isAppearanceLightStatusBars = !darkTheme
        insetsController.isAppearanceLightNavigationBars = !darkTheme
        window.navigationBarColor = navigationBarColor.toArgb()
        if (Build.VERSION.SDK_INT < 26 && !darkTheme) {
            window.navigationBarColor = Color.argb(0x80, 0x1b, 0x1b, 0x1b)
        }
    }
}

@Composable
fun rememberSystemUiController(): SystemUiController {
    val activity = LocalContext.current as ComponentActivity
    return remember {
        val window = activity.window
        val content = activity.findViewById<View>(android.R.id.content)
        SystemUiController(window, WindowCompat.getInsetsController(window, content))
    }
}