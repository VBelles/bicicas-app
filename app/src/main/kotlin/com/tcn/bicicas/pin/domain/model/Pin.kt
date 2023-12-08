package com.tcn.bicicas.pin.domain.model

sealed interface PinResult {
    data class Success(
        val user: String,
        val pin: String,
        val nextPin: String,
        val remainTime: Long,
    ) : PinResult

    data object Unauthorized : PinResult
}