package com.tcn.bicicas.data.model

data class TwoFactorAuth(
    val user: String,
    val secret: String,
)
