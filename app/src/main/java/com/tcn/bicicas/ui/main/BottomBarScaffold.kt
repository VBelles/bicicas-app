package com.tcn.bicicas.ui.main

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


@Composable
fun BottomBarScaffold(
    modifier: Modifier,
    navigationState: NavigationState,
    topBar: @Composable () -> Unit,
    content: @Composable (Screen, PaddingValues) -> Unit,
    iconForScreen: (Screen) -> ImageVector,
) {
    val navController = rememberNavController()

    LaunchedEffect(navigationState.screen) {
        navigate(navController, "route_${navigationState.screen}")
    }

    Scaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = {
            BottomAppBar(tonalElevation = 0.dp) {
                Screen.entries.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(iconForScreen(screen), contentDescription = null) },
                        selected = screen == navigationState.screen,
                        onClick = { navigationState.navigateTo(screen) },
                    )
                }
            }
        },
        content = { padding ->
            val insets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal)
            NavHost(
                modifier = Modifier.padding(padding),
                navController = navController,
                startDestination = "route_${navigationState.initialScreen}"
            ) {
                Screen.entries.forEach { screen ->
                    composable("route_$screen") {
                        content(screen, insets.asPaddingValues())
                    }
                }
            }
        },
    )
}


private fun navigate(navController: NavController, route: String) {
    navController.navigate(route) {
        popUpTo(0) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}