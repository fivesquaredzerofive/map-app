package com.interactivemaps.app

import android.app.Application
import com.interactivemaps.app.data.AppDatabase
import com.interactivemaps.app.data.SettingsDataStore

class MapApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val settingsDataStore by lazy { SettingsDataStore(this) }
}
