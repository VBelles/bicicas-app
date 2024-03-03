package com.tcn.bicicas.pin

import com.tcn.bicicas.common.Clock
import com.tcn.bicicas.common.Store
import com.tcn.bicicas.common.StoreManager
import com.tcn.bicicas.pin.data.authenticate
import com.tcn.bicicas.pin.data.generatePin
import com.tcn.bicicas.pin.data.getTwoFactorAuth
import com.tcn.bicicas.pin.domain.GetPin
import com.tcn.bicicas.pin.domain.Login
import com.tcn.bicicas.pin.domain.Logout
import com.tcn.bicicas.pin.domain.model.TwoFactorAuth
import com.tcn.bicicas.pin.presentation.PinViewModel
import io.ktor.client.HttpClient


interface PinModule {
    val pinViewModel: PinViewModel
}

class PinModuleImpl(
    private val httpClient: () -> HttpClient,
    private val storeManager: () -> StoreManager,
    private val clock: Clock,
    private val oauthBaseUrl: String,
    private val oauthClientId: String,
    private val oauthClientSecret: String,
) : PinModule {

    override val pinViewModel: PinViewModel
        get() {
            val store: Store<TwoFactorAuth?> = storeManager().getStore()
            return PinViewModel(
                getPin = GetPin(store, clock, ::generatePin),
                login = Login(
                    getAuthentication = { username, password ->
                        httpClient().authenticate(
                            baseUrl = oauthBaseUrl,
                            username = username,
                            password = password,
                            clientId = oauthClientId,
                            clientSecret = oauthClientSecret
                        )
                    },
                    getTwoFactorAuth = { token -> httpClient().getTwoFactorAuth(oauthBaseUrl, token) },
                    store = store
                ),
                logout = Logout(store),
            )
        }

}