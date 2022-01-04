package com.tcn.bicicas.ui

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

fun tickerFlow(periodInMillis: Long, initialDelayInMillis: Long = 0L) = flow {
    delay(initialDelayInMillis)
    while (true) {
        emit(Unit)
        delay(periodInMillis)
    }
}