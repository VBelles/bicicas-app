package com.tcn.bicicas.data.datasource.remote

import com.tcn.bicicas.data.model.Token
import com.tcn.bicicas.data.model.TwoFactorAuth
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
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
): Token = post("$baseUrl/oauth/token") {
    parameter("username", username)
    parameter("password", password)
    parameter("client_id", clientId)
    parameter("client_secret", clientSecret)
    parameter("grant_type", "password")
}.body<TokenResponse>().let { response -> Token(response.accessToken) }

@Serializable
private class TwoFactorAuthResponse(
    @SerialName("dasboard") val dashboard: Dashboard
) {
    @Serializable
    class Dashboard(
        @SerialName("user") val user: String,
        @SerialName("secret") val secret: String,
    )
}

suspend fun HttpClient.getTwoFactorAuth(baseUrl: String, token: String) =
    get("$baseUrl/oauth/token") {
        header("Authorization", "Bearer $token")
    }.body<TwoFactorAuthResponse>().let { response ->
        TwoFactorAuth(response.dashboard.user, response.dashboard.secret)
    }