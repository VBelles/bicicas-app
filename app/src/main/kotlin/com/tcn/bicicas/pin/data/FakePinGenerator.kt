package com.tcn.bicicas.pin.data

import kotlin.random.Random


@Suppress("unused")
fun generatePin(secret: String, time: Long, key: String? = null): String =
    Random((secret + time + key).hashCode()).nextInt(100000, 999999).toString()

