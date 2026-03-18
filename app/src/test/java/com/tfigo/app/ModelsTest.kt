package com.tfigo.app

import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.tfigo.app.data.model.*
import org.junit.Test

class ModelsTest {

    private val gson = Gson()

    @Test
    fun `LocationResult deserializes correctly`() {
        val json = """
            {
                "status": {"success": true},
                "name": "Connolly",
                "id": "8220IR0007",
                "shortCode": "999100",
                "coordinate": {"latitude": 53.351252, "longitude": -6.249704},
                "type": "TRAIN_STATION"
            }
        """.trimIndent()

        val result = gson.fromJson(json, LocationResult::class.java)
        assertThat(result.name).isEqualTo("Connolly")
        assertThat(result.id).isEqualTo("8220IR0007")
        assertThat(result.shortCode).isEqualTo("999100")
        assertThat(result.type).isEqualTo("TRAIN_STATION")
        assertThat(result.coordinate?.latitude).isWithin(0.001).of(53.351)
    }

    @Test
    fun `Departure deserializes with all fields`() {
        val json = """
            {
                "destination": "Burlington Road",
                "realTimeDeparture": "2026-03-18T11:20:20.000+00:00",
                "scheduledDeparture": "2026-03-18T11:16:04.000+00:00",
                "cancelled": false,
                "serviceNumber": "38",
                "serviceID": "1 38 c a",
                "transportMode": "BUS",
                "operator": {
                    "operatorCode": "1",
                    "operatorName": "Dublin Bus",
                    "phone": "0818 294 015"
                },
                "vehicle": {
                    "dataFrameRef": "SH2026_03_18_5512",
                    "datedVehicleJourneyRef": "5512_9547"
                }
            }
        """.trimIndent()

        val departure = gson.fromJson(json, Departure::class.java)
        assertThat(departure.destination).isEqualTo("Burlington Road")
        assertThat(departure.serviceNumber).isEqualTo("38")
        assertThat(departure.cancelled).isFalse()
        assertThat(departure.realTimeDeparture).isNotNull()
        assertThat(departure.operator?.operatorName).isEqualTo("Dublin Bus")
        assertThat(departure.vehicle?.dataFrameRef).startsWith("SH2026")
    }

    @Test
    fun `Departure with null realTime uses scheduled`() {
        val departure = Departure(
            destination = "Test",
            serviceNumber = "46A",
            scheduledDeparture = "2026-03-18T12:00:00.000+00:00",
            realTimeDeparture = null
        )
        val displayTime = departure.realTimeDeparture ?: departure.scheduledDeparture
        assertThat(displayTime).isEqualTo("2026-03-18T12:00:00.000+00:00")
    }

    @Test
    fun `DepartureRequest serializes correctly`() {
        val request = DepartureRequest(
            clientTimeZoneOffsetInMS = 0,
            departureDate = "2026-03-18T12:00:00+00:00",
            departureTime = "2026-03-18T12:00:00+00:00",
            stopIds = listOf("8220DB000818"),
            stopType = "BUS_STOP",
            stopName = "Test",
            requestTime = "2026-03-18T12:00:00+00:00"
        )

        val json = gson.toJson(request)
        assertThat(json).contains("8220DB000818")
        assertThat(json).contains("BUS_STOP")
        assertThat(json).contains("DEPARTURE")
    }

    @Test
    fun `FavouriteStop round-trips through JSON`() {
        val fav = FavouriteStop(
            id = "8220DB000818",
            name = "O'Connell Ave",
            shortCode = "818",
            type = "BUS_STOP",
            latitude = 53.358,
            longitude = -6.269
        )

        val json = gson.toJson(fav)
        val restored = gson.fromJson(json, FavouriteStop::class.java)
        assertThat(restored.id).isEqualTo(fav.id)
        assertThat(restored.name).isEqualTo(fav.name)
        assertThat(restored.shortCode).isEqualTo(fav.shortCode)
        assertThat(restored.latitude).isWithin(0.001).of(fav.latitude)
    }

    @Test
    fun `DepartureResponse with error parses`() {
        val json = """{"errorMessage":"Invalid Request","errorType":"UNKNOWN_ERROR"}"""
        val response = gson.fromJson(json, DepartureResponse::class.java)
        assertThat(response.errorMessage).isEqualTo("Invalid Request")
        assertThat(response.stopDepartures).isNull()
    }
}
