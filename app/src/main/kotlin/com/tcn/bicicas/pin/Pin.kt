package com.tcn.bicicas.pin

data class Pin(
    val pin: String,
    val nextPin: String,
    val remainTime: Long,
)