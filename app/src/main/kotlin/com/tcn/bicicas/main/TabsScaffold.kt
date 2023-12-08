package com.tcn.bicicas.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.tcn.bicicas.common.ui.pagerTabIndicatorOffset

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TabsScaffold(
    modifier: Modifier,
    navigationState: NavigationState,
    topBar: @Composable () -> Unit,
    content: @Composable (Screen, PaddingValues) -> Unit,
    iconForScreen: (Screen) -> ImageVector,
) {
    val pagerState = rememberPagerState(
        initialPage = navigationState.initialScreen.ordinal,
        pageCount = { Screen.entries.size }
    )

    LaunchedEffect(pagerState.targetPage) {
        navigationState.navigateTo(Screen.entries[pagerState.targetPage])
    }

    LaunchedEffect(navigationState.screen) {
        if (navigationState.screen.ordinal != pagerState.targetPage) {
            pagerState.animateScrollToPage(navigationState.screen.ordinal)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = topBar,
        contentWindowInsets = WindowInsets(0),
        content = { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                TabRow(
                    selectedTabIndex = navigationState.screen.ordinal,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
                        )
                    },
                    divider = { },
                ) {
                    Screen.entries.forEach { screen ->
                        Tab(
                            selected = navigationState.screen == screen,
                            onClick = { navigationState.navigateTo(screen) },
                            selectedContentColor = MaterialTheme.colorScheme.onSurface,
                            unselectedContentColor = MaterialTheme.colorScheme.onSurface,
                            icon = { Icon(iconForScreen(screen), contentDescription = null) }
                        )
                    }
                }

                val contentPadding = WindowInsets.navigationBars.asPaddingValues()
                HorizontalPager(
                    modifier = Modifier.fillMaxSize(),
                    state = pagerState,
                    pageContent = { page -> content(Screen.entries[page], contentPadding) }
                )
            }
        },
    )
}