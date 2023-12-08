package com.tcn.bicicas.pin.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tcn.bicicas.pin.domain.GetPin
import com.tcn.bicicas.pin.domain.Login
import com.tcn.bicicas.pin.domain.Logout
import com.tcn.bicicas.pin.domain.model.PinResult
import io.ktor.client.plugins.ClientRequestException
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Immutable
sealed interface PinState {

    data object Loading : PinState
    data class LoggedIn(val pinResult: PinResult.Success) : PinState

    data class LoggedOut(
        val loading: Boolean = false,
        val loginError: LoginError? = null,
    ) : PinState

    enum class LoginError {
        WrongUserPass, Network, Unknown
    }
}

class PinViewModel(
    getPin: GetPin,
    private val login: Login,
    private val logout: Logout,
) : ViewModel() {

    private val loginError = MutableStateFlow<PinState.LoginError?>(null)
    private val loginLoading = MutableStateFlow(false)

    val state: StateFlow<PinState> =
        combine(getPin(), loginError, loginLoading) { pinResult, loginError, loginLoading ->
         /*   println(pinResult)
            println(loginError)
            println(loginLoading)*/
            when (pinResult) {
                is PinResult.Success -> PinState.LoggedIn(pinResult)
                is PinResult.Unauthorized -> PinState.LoggedOut(loginLoading, loginError)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), PinState.Loading)

    fun onLogin(username: String, password: String) {
        loginError.value = null
        loginLoading.value = true
        viewModelScope.launch {
            login(username, password).onFailure { error ->
                loginError.value = when (error) {
                    is ClientRequestException -> PinState.LoginError.WrongUserPass
                    is IOException -> PinState.LoginError.Network
                    else -> PinState.LoginError.Unknown
                }
            }
            loginLoading.value = false
        }
    }

    fun onLogout() {
        viewModelScope.launch { logout() }
    }
}