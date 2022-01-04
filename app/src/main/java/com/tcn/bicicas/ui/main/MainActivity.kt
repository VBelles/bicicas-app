package com.tcn.bicicas.ui.main

import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.tcn.bicicas.data.model.Settings
import com.tcn.bicicas.ui.settings.SettingsScreen
import com.tcn.bicicas.ui.settings.SettingsViewModel
import com.tcn.bicicas.ui.stations.StationsViewModel
import com.tcn.bicicas.ui.theme.Theme
import org.koin.androidx.compose.getViewModel

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        installSplashScreen()

        setContent {
            val settingsViewModel: SettingsViewModel = getViewModel()
            val stationsViewModel: StationsViewModel = getViewModel()
            val settings by settingsViewModel.settingsState.collectAsState()

            val darkTheme = settings.theme == Settings.Theme.Dark
                    || (settings.theme == Settings.Theme.System && isSystemInDarkTheme())
            val dynamicColor = settings.dynamicColorEnabled
            val isPortrait = LocalConfiguration.current.orientation == ORIENTATION_PORTRAIT
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

            Theme(darkTheme = darkTheme, dynamicColor = dynamicColor) {
                ProvideWindowInsets {
                    Surface {
                        MainScreen(
                            settings.navigationType,
                            settings.initialScreen.ordinal,
                            stationsViewModel.navigateToMapEvent
                        ) {
                            settingsOpened = true
                        }
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
        }
    }
}

