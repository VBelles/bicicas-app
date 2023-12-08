package com.tcn.bicicas.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class Screen { Pin, List, Map }

data class NavigationState(
    val initialScreen: Screen,
) {
    var screen by mutableStateOf(initialScreen)
        private set

    fun navigateTo(screen: Screen) {
        this.screen = screen
    }
}