package com.tfigo.app.data.repository

import com.tfigo.app.data.api.ApiClient
import com.tfigo.app.data.model.*
import java.text.SimpleDateFormat
import java.util.*

class TfiRepository {

    private val api = ApiClient.api

    suspend fun searchStops(query: String): List<LocationResult> {
        return try {
            api.searchLocations(query)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getDepartures(stop: LocationResult): List<Departure> {
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
            api.getDepartures(request).stopDepartures ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getDeparturesForFavourite(fav: FavouriteStop): List<Departure> {
        val location = LocationResult(
            id = fav.id,
            name = fav.name,
            type = fav.type,
            shortCode = fav.shortCode
        )
        return getDepartures(location)
    }
}
