package com.tcn.bicicas.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tcn.bicicas.ui.theme.BarTonalElevation


@Composable
fun BottomBarScreen(
    initialScreen: Int,
    screens: List<Screen>,
    navigateToMap: Boolean,
    onNavigatedToScreen: (Int) -> Unit,
) {
    val navController = rememberNavController()
    val insets = WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal)
    var selectedScreenIndex by remember { mutableStateOf(initialScreen) }

    LaunchedEffect(navigateToMap) {
        selectedScreenIndex = 2
        navigate(navController, "route_$selectedScreenIndex")
        onNavigatedToScreen(selectedScreenIndex)
    }

    val initialRoute = rememberSaveable { "route_$initialScreen" }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .background(MaterialTheme.colorScheme.background)
        ) {
            NavHost(navController, initialRoute) {
                screens.forEachIndexed { index, screen ->
                    composable("route_$index") {
                        screen.content(insets.asPaddingValues())
                    }
                }
            }
        }

        Surface(
            tonalElevation = BarTonalElevation,
            shadowElevation = 10.dp,
        ) {
            BottomAppBar(tonalElevation = 0.dp, modifier = Modifier.navigationBarsPadding()) {
                screens.forEachIndexed { index, screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        selected = index == selectedScreenIndex,
                        onClick = {
                            selectedScreenIndex = index
                            navigate(navController, "route_$index")
                            onNavigatedToScreen(index)
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            indicatorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
    }
}

private fun navigate(navController: NavController, route: String) {
    navController.navigate(route) {
        popUpTo(0) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}