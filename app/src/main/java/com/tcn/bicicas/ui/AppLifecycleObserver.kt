package com.tcn.bicicas.ui

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow


object AppLifecycleObserver : DefaultLifecycleObserver {

    private val activeSharedFlow = MutableSharedFlow<Boolean>(replay = 1)
    val activeFlow: Flow<Boolean> = activeSharedFlow.asSharedFlow()

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        activeSharedFlow.tryEmit(true)
    }

    override fun onStop(owner: LifecycleOwner) {
        activeSharedFlow.tryEmit(false)
    }
}