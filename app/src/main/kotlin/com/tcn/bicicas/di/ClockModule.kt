package com.tcn.bicicas.di

import com.tcn.bicicas.data.Clock
import org.koin.dsl.module

val clockModule = module {
    single { Clock { System.currentTimeMillis() } }
}