package com.tcn.bicicas.pin.data

import com.tcn.bicicas.common.result
import com.tcn.bicicas.pin.domain.model.TwoFactorAuth
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.prepareGet
import io.ktor.client.request.preparePost
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
private class TokenResponse(
    @SerialName("access_token") val accessToken: String
)

suspend fun HttpClient.authenticate(
    baseUrl: String,
    username: String,
    password: String,
    clientId: String,
    clientSecret: String,
): Result<String> = preparePost("$baseUrl/oauth/token") {
    parameter("username", username)
    parameter("password", password)
    parameter("client_id", clientId)
    parameter("client_secret", clientSecret)
    parameter("grant_type", "password")
}.result { response: TokenResponse -> response.accessToken }

@Serializable
private class TwoFactorAuthResponse(@SerialName("dashboard") val dashboard: Dashboard) {
    @Serializable
    class Dashboard(@SerialName("twofactor") val twoFactor: TwoFactor)

    @Serializable
    class TwoFactor(
        @SerialName("user") val user: String,
        @SerialName("secret") val secret: String,
    )
}

suspend fun HttpClient.getTwoFactorAuth(baseUrl: String, token: String): Result<TwoFactorAuth> =
    prepareGet("$baseUrl/dashboard") {
        header("Authorization", "Bearer $token")
    }.result { response: TwoFactorAuthResponse ->
        TwoFactorAuth(response.dashboard.twoFactor.user, response.dashboard.twoFactor.secret)
    }

