package com.wanderer.repetitortap

import android.app.Application
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.wanderer.repetitortap.data.AppDatabase
import com.wanderer.repetitortap.data.SettingsDataStore
import com.wanderer.repetitortap.data.repositories.AuthRepository
import com.wanderer.repetitortap.data.repositories.TutorRepository

class MapApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val settingsDataStore by lazy { SettingsDataStore(this) }
    val authRepository by lazy { AuthRepository() }
    val tutorRepository by lazy { TutorRepository() }

    override fun onCreate() {
        super.onCreate()
        // Enable Firestore offline persistence
        val firestore = FirebaseFirestore.getInstance()
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
    }
}
