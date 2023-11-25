package com.tcn.bicicas.data.repository

import com.tcn.bicicas.data.andThen
import com.tcn.bicicas.data.datasource.local.LocalStore
import com.tcn.bicicas.data.datasource.remote.SecretApi
import com.tcn.bicicas.data.model.Token
import com.tcn.bicicas.data.model.TwoFactorAuth
import com.tcn.bicicas.data.resultOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import pingenerator.api.PinGenerator
import pingenerator.api.PinGeneratorFactory

class PinRepository(
    private val secretApi: SecretApi,
    private val store: Store<TwoFactorAuth>,
    private val clientId: String,
    private val clientSecret: String,
) {

    private var user: String? = null
    private var pinGenerator: PinGenerator? = null
    private val _authenticatedState = MutableStateFlow(false)
    val authenticatedState: StateFlow<Boolean> = _authenticatedState.asStateFlow()

    init {
        twoFactorStore.get()?.let { twoFactorStore ->
            onTwoFactorAuthObtained(twoFactorStore)
        }
    }

    suspend fun authenticate(username: String, password: String): Result<TwoFactorAuth> {
        return doAuthRequest(username, password)
            .andThen { token -> getTwoFactorAuth(token.value) }
            .onSuccess { twoFactorAuth -> onTwoFactorAuthObtained(twoFactorAuth) }
    }

    fun getPin(time: Long): String? = pinGenerator?.generatePin(time)

    fun logout() {
        pinGenerator = null
        twoFactorStore.clear()
        _authenticatedState.update { false }
    }

    fun getUserNumber(): String? = user

    private suspend fun doAuthRequest(username: String, password: String): Result<Token> {
        return resultOf {
            secretApi.authenticate(
                username = username,
                password = password,
                clientId = clientId,
                clientSecret = clientSecret,
                grantType = "password",
            )
        }.map { (_, token) -> token }
    }

    @Synchronized
    private fun onTwoFactorAuthObtained(twoFactorAuth: TwoFactorAuth) {
        twoFactorStore.save(twoFactorAuth)
        user = twoFactorAuth.user
        pinGenerator = PinGeneratorFactory.getInstance(twoFactorAuth.secret)
        _authenticatedState.update { true }
    }

    private suspend fun getTwoFactorAuth(token: String): Result<TwoFactorAuth> {
        return resultOf { secretApi.getTwoFactorAuth("Bearer $token") }
            .map { (_, twoFactorAuth) -> twoFactorAuth }
    }

}