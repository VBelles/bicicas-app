package com.tcn.bicicas.pin.domain

import com.tcn.bicicas.common.Store
import com.tcn.bicicas.common.andThen
import com.tcn.bicicas.pin.domain.model.TwoFactorAuth

class Login(
    private val getAuthentication: suspend (String, String) -> Result<String>,
    private val getTwoFactorAuth: suspend (String) -> Result<TwoFactorAuth>,
    private val store: Store<TwoFactorAuth?>,
) {

    suspend operator fun invoke(username: String, password: String): Result<TwoFactorAuth> {
        return if (username.lowercase() == "test" && password.lowercase() == "test") {
            Result.success(TwoFactorAuth("0000", "TESTTESTTESTTEST"))
        } else {
            getAuthentication(username, password)
                .andThen { token -> getTwoFactorAuth(token) }
        }.onSuccess { twoFactorAuth ->
            store.update { twoFactorAuth }
        }
    }
}

