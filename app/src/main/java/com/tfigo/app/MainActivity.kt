package com.tfigo.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.tfigo.app.ui.MainViewModel
import com.tfigo.app.ui.screens.DeparturesScreen
import com.tfigo.app.ui.screens.HomeScreen
import com.tfigo.app.ui.theme.TFIGoTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TFIGoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    TFIGoApp(viewModel)
                }
            }
        }
    }

    @Deprecated("Use OnBackPressedDispatcher")
    override fun onBackPressed() {
        if (viewModel.currentStop.value != null) {
            viewModel.goBack()
        } else {
            super.onBackPressed()
        }
    }
}

@Composable
fun TFIGoApp(viewModel: MainViewModel) {
    val currentStop by viewModel.currentStop.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val departures by viewModel.departures.collectAsState()
    val isLoadingDepartures by viewModel.isLoadingDepartures.collectAsState()
    val isFavourite by viewModel.isFavourite.collectAsState()
    val lastUpdated by viewModel.lastUpdated.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val favourites by viewModel.favourites.collectAsState()

    AnimatedContent(
        targetState = currentStop != null,
        transitionSpec = {
            if (targetState) {
                slideInHorizontally { it } + fadeIn() togetherWith
                    slideOutHorizontally { -it / 3 } + fadeOut()
            } else {
                slideInHorizontally { -it / 3 } + fadeIn() togetherWith
                    slideOutHorizontally { it } + fadeOut()
            }
        },
        label = "navigation"
    ) { showDepartures ->
        if (showDepartures && currentStop != null) {
            val stop = currentStop!!
            DeparturesScreen(
                stopName = stop.name,
                stopCode = stop.shortCode,
                stopType = stop.type,
                departures = departures,
                isLoading = isLoadingDepartures,
                isFavourite = isFavourite,
                lastUpdated = lastUpdated,
                errorMessage = errorMessage,
                onBack = { viewModel.goBack() },
                onRefresh = { viewModel.refreshDepartures() },
                onToggleFavourite = { viewModel.toggleFavourite() },
                onClearError = { viewModel.clearError() }
            )
        } else {
            HomeScreen(
                searchQuery = searchQuery,
                onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                searchResults = searchResults,
                isSearching = isSearching,
                favourites = favourites,
                onStopSelected = { viewModel.selectStop(it) },
                onFavouriteSelected = { viewModel.selectFavourite(it) },
                onRemoveFavourite = { viewModel.removeFavourite(it) }
            )
        }
    }
}
