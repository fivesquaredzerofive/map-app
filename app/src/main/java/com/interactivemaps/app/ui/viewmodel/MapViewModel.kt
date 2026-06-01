package com.interactivemaps.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.interactivemaps.app.data.MarkerDao
import com.interactivemaps.app.data.MarkerEntity
import com.interactivemaps.app.data.SettingsDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MapViewModel(
    private val markerDao: MarkerDao,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    val markers: StateFlow<List<MarkerEntity>> = markerDao.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val currentMarkerColor: StateFlow<Long> = settingsDataStore.markerColorFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsDataStore.DEFAULT_COLOR
        )

    fun addMarker(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val count = markers.value.size + 1
            val newMarker = MarkerEntity(
                latitude = latitude,
                longitude = longitude,
                title = "Marker #$count",
                color = currentMarkerColor.value
            )
            markerDao.insert(newMarker)
        }
    }

    fun deleteMarker(marker: MarkerEntity) {
        viewModelScope.launch {
            markerDao.delete(marker)
        }
    }
}

class MapViewModelFactory(
    private val markerDao: MarkerDao,
    private val settingsDataStore: SettingsDataStore
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MapViewModel(markerDao, settingsDataStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
