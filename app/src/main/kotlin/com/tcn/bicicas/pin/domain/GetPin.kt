package com.tcn.bicicas.pin.domain

import com.tcn.bicicas.common.Clock
import com.tcn.bicicas.common.Store
import com.tcn.bicicas.pin.domain.model.PinResult
import com.tcn.bicicas.pin.domain.model.TwoFactorAuth
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive

class GetPin(
    private val store: Store<TwoFactorAuth?>,
    private val clock: Clock,
    private val pinGenerator: (String, Long) -> String
) {
    operator fun invoke(): Flow<PinResult> = channelFlow {
        store.updates().collectLatest { auth ->
            if (auth == null) send(PinResult.Unauthorized)
            else pinFlow(auth).collectLatest(::send)
        }
    }

    private fun pinFlow(auth: TwoFactorAuth) = flow {
        while (currentCoroutineContext().isActive) {
            val time = clock.millis()
            val pin = pinGenerator(auth.secret, time)
            val nextPin = pinGenerator(auth.secret, time + 30_000)
            val epochPhase = time / 30_000
            val timerTarget = (epochPhase * 30 + 30) * 1000L
            val remainTime = timerTarget - clock.millis()
            emit(PinResult.Success(auth.user, pin, nextPin, remainTime))
            delay(remainTime)
        }
    }
}
