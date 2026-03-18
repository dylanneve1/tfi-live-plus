package com.tfigo.app.data.api

import com.tfigo.app.data.model.DepartureRequest
import com.tfigo.app.data.model.DepartureResponse
import com.tfigo.app.data.model.LocationResult
import retrofit2.http.*

interface TfiApi {

    @GET("locationLookup")
    suspend fun searchLocations(
        @Query("query") query: String,
        @Query("allowedTypes") allowedTypes: String = "BUS_STOP,TRAIN_STATION,TRAM_STOP,TRAM_STOP_AREA,COACH_STOP,FERRY_PORT",
        @Query("language") language: String = "en"
    ): List<LocationResult>

    @POST("departures")
    suspend fun getDepartures(
        @Body request: DepartureRequest
    ): DepartureResponse
}
