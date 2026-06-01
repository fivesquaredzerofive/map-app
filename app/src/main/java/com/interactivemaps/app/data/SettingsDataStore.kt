package com.interactivemaps.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {
    companion object {
        val MARKER_COLOR = longPreferencesKey("marker_color")
        // Default color is Samsung Blue (0xFF0381FE)
        const val DEFAULT_COLOR = 0xFF0381FE
    }

    val markerColorFlow: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[MARKER_COLOR] ?: DEFAULT_COLOR
        }

    suspend fun saveMarkerColor(color: Long) {
        context.dataStore.edit { preferences ->
            preferences[MARKER_COLOR] = color
        }
    }
}
