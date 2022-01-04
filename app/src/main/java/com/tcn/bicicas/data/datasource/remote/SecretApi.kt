package com.tcn.bicicas.data.datasource.remote

import com.tcn.bicicas.data.model.Token
import com.tcn.bicicas.data.model.TwoFactorAuth
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface SecretApi {

    @POST("oauth/token")
    suspend fun authenticate(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("client_id") clientId: String,
        @Query("client_secret") clientSecret: String,
        @Query("grant_type") grantType: String,
    ): Response<Token>

    @GET("dashboard")
    suspend fun getTwoFactorAuth(@Header("Authorization") token: String): Response<TwoFactorAuth>

}