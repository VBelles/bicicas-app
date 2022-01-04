package com.tcn.bicicas.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class Settings(
    val initialScreen: Screen = Screen.Pin,
    val theme: Theme = Theme.System,
    val navigationType: NavigationType = NavigationType.BottomBar,
    val dynamicColorEnabled: Boolean = false,
) {

    enum class Screen { Pin, Stations, Map }

    enum class Theme { System, Light, Dark }

    enum class NavigationType { BottomBar, Tabs }
}