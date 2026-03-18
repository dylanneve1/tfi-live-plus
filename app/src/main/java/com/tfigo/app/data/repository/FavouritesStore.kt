package com.tfigo.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tfigo.app.data.model.FavouriteStop
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "favourites")

class FavouritesStore(private val context: Context) {

    private val gson = Gson()
    private val key = stringPreferencesKey("favourite_stops")

    val favourites: Flow<List<FavouriteStop>> = context.dataStore.data.map { prefs ->
        val json = prefs[key] ?: "[]"
        val type = object : TypeToken<List<FavouriteStop>>() {}.type
        gson.fromJson(json, type) ?: emptyList()
    }

    suspend fun addFavourite(stop: FavouriteStop) {
        context.dataStore.edit { prefs ->
            val current = getCurrentList(prefs)
            if (current.none { it.id == stop.id }) {
                val updated = listOf(stop) + current
                prefs[key] = gson.toJson(updated)
            }
        }
    }

    suspend fun removeFavourite(stopId: String) {
        context.dataStore.edit { prefs ->
            val current = getCurrentList(prefs)
            val updated = current.filter { it.id != stopId }
            prefs[key] = gson.toJson(updated)
        }
    }

    suspend fun isFavourite(stopId: String): Boolean {
        var result = false
        context.dataStore.edit { prefs ->
            result = getCurrentList(prefs).any { it.id == stopId }
        }
        return result
    }

    private fun getCurrentList(prefs: Preferences): List<FavouriteStop> {
        val json = prefs[key] ?: "[]"
        val type = object : TypeToken<List<FavouriteStop>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
}
