package com.tfigo.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tfigo.app.data.model.*
import com.tfigo.app.data.repository.ApiResult
import com.tfigo.app.data.repository.FavouritesStore
import com.tfigo.app.data.repository.TfiRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TfiRepository()
    private val favouritesStore = FavouritesStore(application)

    // Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _searchResults = MutableStateFlow<List<LocationResult>>(emptyList())
    val searchResults: StateFlow<List<LocationResult>> = _searchResults

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    // Departures
    private val _currentStop = MutableStateFlow<LocationResult?>(null)
    val currentStop: StateFlow<LocationResult?> = _currentStop

    private val _departures = MutableStateFlow<List<Departure>>(emptyList())
    val departures: StateFlow<List<Departure>> = _departures

    private val _isLoadingDepartures = MutableStateFlow(false)
    val isLoadingDepartures: StateFlow<Boolean> = _isLoadingDepartures

    private val _isFavourite = MutableStateFlow(false)
    val isFavourite: StateFlow<Boolean> = _isFavourite

    private val _lastUpdated = MutableStateFlow("")
    val lastUpdated: StateFlow<String> = _lastUpdated

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // Favourites
    val favourites: StateFlow<List<FavouriteStop>> = favouritesStore.favourites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Auto-refresh
    private var refreshJob: Job? = null
    private var searchJob: Job? = null

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()

        if (query.length < 2) {
            _searchResults.value = emptyList()
            _isSearching.value = false
            return
        }

        searchJob = viewModelScope.launch {
            delay(300)
            _isSearching.value = true
            when (val result = repository.searchStops(query)) {
                is ApiResult.Success -> _searchResults.value = result.data
                is ApiResult.Error -> {
                    _searchResults.value = emptyList()
                    _errorMessage.value = result.message
                }
            }
            _isSearching.value = false
        }
    }

    fun selectStop(stop: LocationResult) {
        _currentStop.value = stop
        _searchQuery.value = ""
        _searchResults.value = emptyList()
        _errorMessage.value = null
        loadDepartures(stop)
        checkFavourite(stop.id)
        startAutoRefresh()
    }

    fun selectFavourite(fav: FavouriteStop) {
        val location = LocationResult(
            id = fav.id,
            name = fav.name,
            type = fav.type,
            shortCode = fav.shortCode,
            coordinate = Coordinate(fav.latitude, fav.longitude)
        )
        selectStop(location)
    }

    fun goBack() {
        _currentStop.value = null
        _departures.value = emptyList()
        _lastUpdated.value = ""
        _errorMessage.value = null
        stopAutoRefresh()
    }

    fun refreshDepartures() {
        _currentStop.value?.let { loadDepartures(it) }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun toggleFavourite() {
        val stop = _currentStop.value ?: return
        viewModelScope.launch {
            if (_isFavourite.value) {
                favouritesStore.removeFavourite(stop.id)
                _isFavourite.value = false
            } else {
                favouritesStore.addFavourite(
                    FavouriteStop(
                        id = stop.id,
                        name = stop.name,
                        shortCode = stop.shortCode,
                        type = stop.type,
                        latitude = stop.coordinate?.latitude ?: 0.0,
                        longitude = stop.coordinate?.longitude ?: 0.0
                    )
                )
                _isFavourite.value = true
            }
        }
    }

    fun removeFavourite(stopId: String) {
        viewModelScope.launch {
            favouritesStore.removeFavourite(stopId)
        }
    }

    private fun loadDepartures(stop: LocationResult) {
        viewModelScope.launch {
            _isLoadingDepartures.value = true
            _errorMessage.value = null
            when (val result = repository.getDepartures(stop)) {
                is ApiResult.Success -> {
                    _departures.value = result.data
                    _lastUpdated.value = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                }
                is ApiResult.Error -> {
                    if (_departures.value.isEmpty()) {
                        _errorMessage.value = result.message
                    }
                    // Keep showing old data if we have it
                }
            }
            _isLoadingDepartures.value = false
        }
    }

    private fun checkFavourite(stopId: String) {
        viewModelScope.launch {
            _isFavourite.value = favouritesStore.isFavourite(stopId)
        }
    }

    private fun startAutoRefresh() {
        stopAutoRefresh()
        refreshJob = viewModelScope.launch {
            while (isActive) {
                delay(30_000)
                _currentStop.value?.let { loadDepartures(it) }
            }
        }
    }

    private fun stopAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopAutoRefresh()
    }
}
