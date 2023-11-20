package com.tcn.bicicas.ui.pin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tcn.bicicas.data.Clock
import com.tcn.bicicas.data.model.HttpError
import com.tcn.bicicas.data.model.NetworkError
import com.tcn.bicicas.data.repository.PinRepository
import com.tcn.bicicas.ui.tickerFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class PinViewModel(
    private val pinRepository: PinRepository,
    private val clock: Clock,
) : ViewModel() {

    private val _pinState = MutableStateFlow(PinState())
    val pinState: StateFlow<PinState> = _pinState.asStateFlow()

    init {

        // Update auth state
        pinRepository.authenticatedState.onEach { loggedIn ->
            _pinState.update { state -> state.copy(loggedIn = loggedIn) }
        }.launchIn(viewModelScope)

        // Update progress every 500ms and pin on epoch changes (every 30s)
        tickerFlow(500)
            .combine(pinRepository.authenticatedState) { _, loggedIn -> loggedIn }
            .filter { loggedIn -> loggedIn }
            .map { getTimeEpoch() }
            .onEach { (time, epoch) -> updateProgress(time, epoch) }
            .distinctUntilChangedBy { (_, epoch) -> epoch to pinRepository.getUserNumber() }
            .onEach { (time, _) -> updatePin(time) }
            .launchIn(viewModelScope)
    }

    private fun getTimeEpoch(): Pair<Long, Long> {
        val time = clock.millis()
        val epoch = (time / 1000.0).toLong() / 30
        return time to epoch
    }

    private fun updateProgress(time: Long, epoch: Long) = _pinState.update { state ->
        val timerTarget = (epoch * 30 + 30) * 1000L
        val remainTime = (timerTarget - time) / 1000.0
        val progress = remainTime.roundToInt() / 30f
        state.copy(
            timeText = remainTime.roundToInt().toString(),
            progress = progress,
        )
    }

    private fun updatePin(time: Long) = _pinState.update { state ->
        state.copy(
            pin = pinRepository.getPin(time),
            nextPin = pinRepository.getPin(time + 30_000),
            userNumber = pinRepository.getUserNumber(),
        )
    }

    fun login(username: String, password: String) {
        _pinState.update { it.copy(loading = true, loginError = null) }
        viewModelScope.launch {
            pinRepository.authenticate(username, password)
                .onSuccess { twoFactorAuth ->
                    _pinState.update { state ->
                        state.copy(
                            loggedIn = true,
                            loading = false,
                            userNumber = twoFactorAuth.user
                        )
                    }
                }.onFailure { error ->
                    val loginError = when (error) {
                        is HttpError -> PinState.LoginError.WrongUserPass
                        is NetworkError -> PinState.LoginError.Network
                        else -> PinState.LoginError.Unknown
                    }
                    _pinState.update { state ->
                        state.copy(loginError = loginError, loading = false)
                    }
                }
        }
    }

    fun logout() {
        pinRepository.logout()
    }


}