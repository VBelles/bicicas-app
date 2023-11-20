package com.tcn.bicicas.data.datasource.remote

import com.tcn.bicicas.data.model.Station
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers

interface StationApi {

    @GET("bench_status")
    suspend fun getStations(): Response<Array<Station>>

    @Headers("force-cache: true")
    @GET("bench_status")
    suspend fun getStationsFromCache(): Response<Array<Station>>

}