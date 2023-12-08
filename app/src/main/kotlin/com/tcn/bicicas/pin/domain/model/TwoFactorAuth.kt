package com.tcn.bicicas.pin.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class TwoFactorAuth(
    val user: String,
    val secret: String,
)