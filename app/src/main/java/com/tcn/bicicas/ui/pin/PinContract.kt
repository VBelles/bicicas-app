package com.tcn.bicicas.ui.pin

import androidx.compose.runtime.Immutable


@Immutable
data class PinState(
    val timeText: String? = null,
    val progress: Float = 0f,
    val pin: String? = null,
    val nextPin: String? = null,
    val userNumber: String? = null,
    val loading: Boolean = false,
    val loggedIn: Boolean = false,
    val loginError: LoginError? = null,
) {
    enum class LoginError {
        WrongUserPass, Network, Unknown
    }
}
