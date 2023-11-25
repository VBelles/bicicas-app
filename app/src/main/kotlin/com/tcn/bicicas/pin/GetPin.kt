package com.tcn.bicicas.pin

import com.tcn.bicicas.data.datasource.Store
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetPin(
    private val secretStore: Store<String>,
    private val epochInMillis: () -> Long,
    private val pinGenerator: (String, Long) -> String,
) {
    operator fun invoke(): Flow<Pin> = flow {
        while (true) {
            val secret = secretStore.get()
            val time = epochInMillis()
            val pin = pinGenerator(secret, time)
            val nextPin = pinGenerator(secret, time + 30_000)
            val epochPhase = (time / 1000.0).toLong() / 30
            val timerTarget = (epochPhase * 30 + 30) * 1000L
            val remainTime = timerTarget - epochInMillis()
            emit(Pin(pin, nextPin, remainTime))
            delay(remainTime)
        }
    }
}
