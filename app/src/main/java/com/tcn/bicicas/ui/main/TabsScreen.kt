package com.tcn.bicicas.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import com.tcn.bicicas.ui.theme.BarTonalElevation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun TabsScreen(
    screens: List<Screen>,
    initialScreen: Int,
    navigateToMapEvent: Flow<String>,
    onNavigatedToScreen: (Int) -> Unit
) {
    val pagerState = rememberPagerState(initialScreen)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(navigateToMapEvent) {
        navigateToMapEvent.collect {
            pagerState.animateScrollToPage(2)
            onNavigatedToScreen(2)
        }
    }

    Column(Modifier.fillMaxSize()) {
        Surface(tonalElevation = BarTonalElevation) {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.pagerTabIndicatorOffset(
                            pagerState,
                            tabPositions
                        )
                    )
                },
                backgroundColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                screens.forEachIndexed { index, screen ->
                    Tab(
                        selected = index == pagerState.currentPage,
                        onClick = {
                            coroutineScope.launch { pagerState.animateScrollToPage(index) }
                            onNavigatedToScreen(index)
                        },
                        selectedContentColor = MaterialTheme.colorScheme.onSurface,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurface,
                        icon = { Icon(screen.icon, contentDescription = null) }
                    )
                }
            }
        }

        val contentPadding = WindowInsets.navigationBars.asPaddingValues()
        HorizontalPager(
            count = screens.size,
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            screens[page].content(contentPadding)
        }
    }
}