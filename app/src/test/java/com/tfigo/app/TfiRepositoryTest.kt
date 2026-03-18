package com.tfigo.app

import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.tfigo.app.data.api.TfiApi
import com.tfigo.app.data.model.*
import com.tfigo.app.data.repository.ApiResult
import com.tfigo.app.data.repository.TfiRepository
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TfiRepositoryTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var api: TfiApi
    private lateinit var repository: TfiRepository

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        api = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TfiApi::class.java)

        repository = TfiRepository(api)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `searchStops returns results on success`() = runTest {
        val mockResults = listOf(
            LocationResult(
                status = Status(true),
                name = "Connolly Station",
                id = "8220IR0007",
                shortCode = "999100",
                type = "TRAIN_STATION"
            )
        )
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(Gson().toJson(mockResults))
                .addHeader("Content-Type", "application/json")
        )

        val result = repository.searchStops("connolly")
        assertThat(result).isInstanceOf(ApiResult.Success::class.java)
        val data = (result as ApiResult.Success).data
        assertThat(data).hasSize(1)
        assertThat(data[0].name).isEqualTo("Connolly Station")
        assertThat(data[0].type).isEqualTo("TRAIN_STATION")
    }

    @Test
    fun `searchStops returns empty list for no results`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("[]")
                .addHeader("Content-Type", "application/json")
        )

        val result = repository.searchStops("xyznonexistent")
        assertThat(result).isInstanceOf(ApiResult.Success::class.java)
        assertThat((result as ApiResult.Success).data).isEmpty()
    }

    @Test
    fun `searchStops returns error on network failure`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))

        val result = repository.searchStops("connolly")
        assertThat(result).isInstanceOf(ApiResult.Error::class.java)
    }

    @Test
    fun `getDepartures returns departures on success`() = runTest {
        val mockResponse = DepartureResponse(
            status = Status(true),
            stopDepartures = listOf(
                Departure(
                    destination = "Burlington Road",
                    serviceNumber = "38",
                    transportMode = "BUS",
                    scheduledDeparture = "2026-03-18T12:00:00.000+00:00",
                    realTimeDeparture = "2026-03-18T12:02:00.000+00:00",
                    operator = Operator(operatorName = "Dublin Bus")
                ),
                Departure(
                    destination = "Donnybrook",
                    serviceNumber = "11B",
                    transportMode = "BUS",
                    scheduledDeparture = "2026-03-18T12:10:00.000+00:00",
                    cancelled = true
                )
            )
        )
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(Gson().toJson(mockResponse))
                .addHeader("Content-Type", "application/json")
        )

        val stop = LocationResult(id = "8220DB000818", name = "Test Stop", type = "BUS_STOP")
        val result = repository.getDepartures(stop)

        assertThat(result).isInstanceOf(ApiResult.Success::class.java)
        val data = (result as ApiResult.Success).data
        assertThat(data).hasSize(2)
        assertThat(data[0].serviceNumber).isEqualTo("38")
        assertThat(data[0].destination).isEqualTo("Burlington Road")
        assertThat(data[0].realTimeDeparture).isNotNull()
        assertThat(data[1].cancelled).isTrue()
    }

    @Test
    fun `getDepartures returns error when API returns error`() = runTest {
        val mockResponse = DepartureResponse(
            errorMessage = "Invalid Request"
        )
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(Gson().toJson(mockResponse))
                .addHeader("Content-Type", "application/json")
        )

        val stop = LocationResult(id = "invalid", name = "Bad Stop", type = "BUS_STOP")
        val result = repository.getDepartures(stop)

        assertThat(result).isInstanceOf(ApiResult.Error::class.java)
        assertThat((result as ApiResult.Error).message).contains("Invalid Request")
    }

    @Test
    fun `getDepartures returns empty list when no departures`() = runTest {
        val mockResponse = DepartureResponse(
            status = Status(true),
            stopDepartures = emptyList()
        )
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(Gson().toJson(mockResponse))
                .addHeader("Content-Type", "application/json")
        )

        val stop = LocationResult(id = "8220DB000818", name = "Empty Stop", type = "BUS_STOP")
        val result = repository.getDepartures(stop)

        assertThat(result).isInstanceOf(ApiResult.Success::class.java)
        assertThat((result as ApiResult.Success).data).isEmpty()
    }

    @Test
    fun `getDepartures sends correct request body`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(Gson().toJson(DepartureResponse(stopDepartures = emptyList())))
                .addHeader("Content-Type", "application/json")
        )

        val stop = LocationResult(id = "8220DB000818", name = "O'Connell Ave", type = "BUS_STOP")
        repository.getDepartures(stop)

        val request = mockWebServer.takeRequest()
        assertThat(request.method).isEqualTo("POST")
        assertThat(request.path).isEqualTo("/departures")

        val body = request.body.readUtf8()
        assertThat(body).contains("8220DB000818")
        assertThat(body).contains("BUS_STOP")
        assertThat(body).contains("DEPARTURE")
    }

    @Test
    fun `searchStops sends correct query params`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("[]")
                .addHeader("Content-Type", "application/json")
        )

        repository.searchStops("heuston")

        val request = mockWebServer.takeRequest()
        assertThat(request.method).isEqualTo("GET")
        assertThat(request.path).contains("query=heuston")
        assertThat(request.path).contains("allowedTypes=")
        assertThat(request.path).contains("language=en")
    }
}
