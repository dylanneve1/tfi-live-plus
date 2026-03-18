package com.tfigo.app.data.model

import com.google.gson.annotations.SerializedName

// Location search
data class LocationResult(
    val status: Status? = null,
    val name: String = "",
    val id: String = "",
    val shortCode: String? = null,
    val coordinate: Coordinate? = null,
    val type: String = ""
)

data class Status(val success: Boolean = false)

data class Coordinate(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

// Departures
data class DepartureRequest(
    val clientTimeZoneOffsetInMS: Long,
    val departureDate: String,
    val departureTime: String,
    val stopIds: List<String>,
    val stopType: String,
    val stopName: String,
    val requestTime: String,
    val departureOrArrival: String = "DEPARTURE",
    val refresh: Boolean = false
)

data class DepartureResponse(
    val status: Status? = null,
    val stopDepartures: List<Departure>? = null,
    val errorMessage: String? = null
)

data class Departure(
    val destination: String = "",
    val realTimeDeparture: String? = null,
    val scheduledDeparture: String? = null,
    val cancelled: Boolean = false,
    val serviceNumber: String = "",
    val serviceID: String = "",
    val serviceDisplayName: String? = null,
    val serviceDirection: String? = null,
    val operator: Operator? = null,
    val transportMode: String = "BUS",
    val vehicle: Vehicle? = null,
    val stopRef: String? = null
)

data class Operator(
    val operatorCode: String? = null,
    val operatorName: String? = null,
    val phone: String? = null,
    val url: String? = null
)

data class Vehicle(
    val reference: String? = null,
    val location: VehicleLocation? = null,
    @SerializedName("dataFrameRef") val dataFrameRef: String? = null,
    @SerializedName("datedVehicleJourneyRef") val datedVehicleJourneyRef: String? = null
)

data class VehicleLocation(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

// Favourite stop (for local storage)
data class FavouriteStop(
    val id: String,
    val name: String,
    val shortCode: String? = null,
    val type: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)
