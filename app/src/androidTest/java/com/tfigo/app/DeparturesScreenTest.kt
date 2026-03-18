package com.tfigo.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.tfigo.app.data.model.Departure
import com.tfigo.app.data.model.Operator
import com.tfigo.app.ui.screens.DeparturesScreen
import com.tfigo.app.ui.theme.TFIGoTheme
import org.junit.Rule
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

class DeparturesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun futureTime(minutesFromNow: Int): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US)
        val cal = Calendar.getInstance()
        cal.add(Calendar.MINUTE, minutesFromNow)
        return sdf.format(cal.time)
    }

    @Test
    fun stopName_isDisplayed() {
        composeTestRule.setContent {
            TFIGoTheme {
                DeparturesScreen(
                    stopName = "O'Connell Avenue",
                    stopCode = "818",
                    stopType = "BUS_STOP",
                    departures = emptyList(),
                    isLoading = false,
                    isFavourite = false,
                    lastUpdated = "",
                    errorMessage = null,
                    onBack = {},
                    onRefresh = {},
                    onToggleFavourite = {},
                    onClearError = {}
                )
            }
        }

        composeTestRule.onNodeWithText("O'Connell Avenue").assertIsDisplayed()
    }

    @Test
    fun loading_showsIndicator() {
        composeTestRule.setContent {
            TFIGoTheme {
                DeparturesScreen(
                    stopName = "Test",
                    stopCode = null,
                    stopType = "BUS_STOP",
                    departures = emptyList(),
                    isLoading = true,
                    isFavourite = false,
                    lastUpdated = "",
                    errorMessage = null,
                    onBack = {},
                    onRefresh = {},
                    onToggleFavourite = {},
                    onClearError = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Loading departures...").assertIsDisplayed()
    }

    @Test
    fun departures_displayCorrectly() {
        val departures = listOf(
            Departure(
                destination = "Burlington Road",
                serviceNumber = "38",
                transportMode = "BUS",
                scheduledDeparture = futureTime(5),
                realTimeDeparture = futureTime(7),
                operator = Operator(operatorName = "Dublin Bus")
            ),
            Departure(
                destination = "Donnybrook",
                serviceNumber = "11B",
                transportMode = "BUS",
                scheduledDeparture = futureTime(10),
                operator = Operator(operatorName = "Dublin Bus")
            )
        )

        composeTestRule.setContent {
            TFIGoTheme {
                DeparturesScreen(
                    stopName = "Test Stop",
                    stopCode = "818",
                    stopType = "BUS_STOP",
                    departures = departures,
                    isLoading = false,
                    isFavourite = false,
                    lastUpdated = "12:00:00",
                    errorMessage = null,
                    onBack = {},
                    onRefresh = {},
                    onToggleFavourite = {},
                    onClearError = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Burlington Road").assertIsDisplayed()
        composeTestRule.onNodeWithText("Donnybrook").assertIsDisplayed()
        composeTestRule.onNodeWithText("38").assertIsDisplayed()
        composeTestRule.onNodeWithText("11B").assertIsDisplayed()
        composeTestRule.onNodeWithText("Dublin Bus").assertExists()
    }

    @Test
    fun cancelledDeparture_showsCancelled() {
        val departures = listOf(
            Departure(
                destination = "Cancelled Route",
                serviceNumber = "99",
                transportMode = "BUS",
                scheduledDeparture = futureTime(5),
                cancelled = true
            )
        )

        composeTestRule.setContent {
            TFIGoTheme {
                DeparturesScreen(
                    stopName = "Test",
                    stopCode = null,
                    stopType = "BUS_STOP",
                    departures = departures,
                    isLoading = false,
                    isFavourite = false,
                    lastUpdated = "",
                    errorMessage = null,
                    onBack = {},
                    onRefresh = {},
                    onToggleFavourite = {},
                    onClearError = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Cancelled").assertIsDisplayed()
    }

    @Test
    fun filterChips_shownWithMultipleServices() {
        val departures = listOf(
            Departure(destination = "A", serviceNumber = "38", scheduledDeparture = futureTime(5)),
            Departure(destination = "B", serviceNumber = "11B", scheduledDeparture = futureTime(10))
        )

        composeTestRule.setContent {
            TFIGoTheme {
                DeparturesScreen(
                    stopName = "Test",
                    stopCode = null,
                    stopType = "BUS_STOP",
                    departures = departures,
                    isLoading = false,
                    isFavourite = false,
                    lastUpdated = "",
                    errorMessage = null,
                    onBack = {},
                    onRefresh = {},
                    onToggleFavourite = {},
                    onClearError = {}
                )
            }
        }

        composeTestRule.onNodeWithText("All").assertIsDisplayed()
        composeTestRule.onNodeWithText("38").assertIsDisplayed()
        composeTestRule.onNodeWithText("11B").assertIsDisplayed()
    }

    @Test
    fun noDepartures_showsEmptyState() {
        composeTestRule.setContent {
            TFIGoTheme {
                DeparturesScreen(
                    stopName = "Empty Stop",
                    stopCode = null,
                    stopType = "BUS_STOP",
                    departures = emptyList(),
                    isLoading = false,
                    isFavourite = false,
                    lastUpdated = "",
                    errorMessage = null,
                    onBack = {},
                    onRefresh = {},
                    onToggleFavourite = {},
                    onClearError = {}
                )
            }
        }

        composeTestRule.onNodeWithText("No departures").assertIsDisplayed()
        composeTestRule.onNodeWithText("Try again").assertIsDisplayed()
    }

    @Test
    fun backButton_callsOnBack() {
        var backCalled = false

        composeTestRule.setContent {
            TFIGoTheme {
                DeparturesScreen(
                    stopName = "Test",
                    stopCode = null,
                    stopType = "BUS_STOP",
                    departures = emptyList(),
                    isLoading = false,
                    isFavourite = false,
                    lastUpdated = "",
                    errorMessage = null,
                    onBack = { backCalled = true },
                    onRefresh = {},
                    onToggleFavourite = {},
                    onClearError = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Back").performClick()
        assert(backCalled)
    }
}
