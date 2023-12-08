package com.tcn.bicicas.settings.domain

import kotlinx.serialization.Serializable

@Serializable
data class Settings(
    val initialScreen: InitialScreen = InitialScreen.Last,
    val lastScreen: Int = InitialScreen.Pin.id,
    val theme: Theme = Theme.System,
    val navigationType: NavigationType = NavigationType.BottomBar,
    val dynamicColorEnabled: Boolean = false,
) {

    enum class InitialScreen(val id: Int) { Last(-1), Pin(0), Stations(1), Map(2) }

    enum class Theme { System, Light, Dark }

    enum class NavigationType { BottomBar, Tabs }
}