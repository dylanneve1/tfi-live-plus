package com.tfigo.app.data.repository

import com.tfigo.app.data.api.TfiApi
import com.tfigo.app.data.api.ApiClient
import com.tfigo.app.data.model.*
import java.text.SimpleDateFormat
import java.util.*

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val exception: Exception? = null) : ApiResult<Nothing>()
}

class TfiRepository(private val api: TfiApi = ApiClient.api) {

    suspend fun searchStops(query: String): ApiResult<List<LocationResult>> {
        return try {
            val results = api.searchLocations(query)
            ApiResult.Success(results)
        } catch (e: Exception) {
            ApiResult.Error("Failed to search: ${e.localizedMessage}", e)
        }
    }

    suspend fun getDepartures(stop: LocationResult): ApiResult<List<Departure>> {
        return try {
            val now = Date()
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US)
            sdf.timeZone = TimeZone.getDefault()
            val timeStr = sdf.format(now)

            val request = DepartureRequest(
                clientTimeZoneOffsetInMS = TimeZone.getDefault().rawOffset.toLong(),
                departureDate = timeStr,
                departureTime = timeStr,
                stopIds = listOf(stop.id),
                stopType = stop.type,
                stopName = stop.name,
                requestTime = timeStr
            )
            val response = api.getDepartures(request)
            if (response.errorMessage != null) {
                ApiResult.Error(response.errorMessage)
            } else {
                ApiResult.Success(response.stopDepartures ?: emptyList())
            }
        } catch (e: Exception) {
            ApiResult.Error("Failed to load departures: ${e.localizedMessage}", e)
        }
    }

    suspend fun getDeparturesForFavourite(fav: FavouriteStop): ApiResult<List<Departure>> {
        val location = LocationResult(
            id = fav.id,
            name = fav.name,
            type = fav.type,
            shortCode = fav.shortCode
        )
        return getDepartures(location)
    }
}
