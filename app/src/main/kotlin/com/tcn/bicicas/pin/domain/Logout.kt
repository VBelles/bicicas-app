package com.tcn.bicicas.pin.domain

import com.tcn.bicicas.common.Store
import com.tcn.bicicas.pin.domain.model.TwoFactorAuth

class Logout(
    private val store: Store<TwoFactorAuth?>,
) {

    suspend operator fun invoke() = store.update { null }
}