package com.tfigo.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.tfigo.app.data.model.FavouriteStop
import com.tfigo.app.data.model.LocationResult
import com.tfigo.app.data.model.Status
import com.tfigo.app.ui.screens.HomeScreen
import com.tfigo.app.ui.theme.TFIGoTheme
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun searchBar_isDisplayed() {
        composeTestRule.setContent {
            TFIGoTheme {
                HomeScreen(
                    searchQuery = "",
                    onSearchQueryChange = {},
                    searchResults = emptyList(),
                    isSearching = false,
                    favourites = emptyList(),
                    onStopSelected = {},
                    onFavouriteSelected = {},
                    onRemoveFavourite = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Search stops, stations...").assertIsDisplayed()
    }

    @Test
    fun emptyState_shownWhenNoFavourites() {
        composeTestRule.setContent {
            TFIGoTheme {
                HomeScreen(
                    searchQuery = "",
                    onSearchQueryChange = {},
                    searchResults = emptyList(),
                    isSearching = false,
                    favourites = emptyList(),
                    onStopSelected = {},
                    onFavouriteSelected = {},
                    onRemoveFavourite = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Find your stop").assertIsDisplayed()
    }

    @Test
    fun favourites_displayedWhenPresent() {
        val favourites = listOf(
            FavouriteStop(
                id = "8220DB000818",
                name = "O'Connell Avenue",
                shortCode = "818",
                type = "BUS_STOP"
            ),
            FavouriteStop(
                id = "8220IR0007",
                name = "Connolly Station",
                shortCode = "999100",
                type = "TRAIN_STATION"
            )
        )

        composeTestRule.setContent {
            TFIGoTheme {
                HomeScreen(
                    searchQuery = "",
                    onSearchQueryChange = {},
                    searchResults = emptyList(),
                    isSearching = false,
                    favourites = favourites,
                    onStopSelected = {},
                    onFavouriteSelected = {},
                    onRemoveFavourite = {}
                )
            }
        }

        composeTestRule.onNodeWithText("FAVOURITES").assertIsDisplayed()
        composeTestRule.onNodeWithText("O'Connell Avenue").assertIsDisplayed()
        composeTestRule.onNodeWithText("Connolly Station").assertIsDisplayed()
    }

    @Test
    fun searchResults_displayedWhenSearching() {
        val results = listOf(
            LocationResult(
                status = Status(true),
                name = "Heuston Station",
                id = "8220IR0020",
                shortCode = "999200",
                type = "TRAIN_STATION"
            )
        )

        composeTestRule.setContent {
            TFIGoTheme {
                HomeScreen(
                    searchQuery = "heuston",
                    onSearchQueryChange = {},
                    searchResults = results,
                    isSearching = false,
                    favourites = emptyList(),
                    onStopSelected = {},
                    onFavouriteSelected = {},
                    onRemoveFavourite = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Heuston Station").assertIsDisplayed()
    }

    @Test
    fun favouriteClick_callsCallback() {
        var clickedId = ""
        val fav = FavouriteStop(
            id = "8220DB000818",
            name = "O'Connell Avenue",
            shortCode = "818",
            type = "BUS_STOP"
        )

        composeTestRule.setContent {
            TFIGoTheme {
                HomeScreen(
                    searchQuery = "",
                    onSearchQueryChange = {},
                    searchResults = emptyList(),
                    isSearching = false,
                    favourites = listOf(fav),
                    onStopSelected = {},
                    onFavouriteSelected = { clickedId = it.id },
                    onRemoveFavourite = {}
                )
            }
        }

        composeTestRule.onNodeWithText("O'Connell Avenue").performClick()
        assert(clickedId == "8220DB000818")
    }
}
