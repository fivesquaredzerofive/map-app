package com.wanderer.repetitortap.data.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.wanderer.repetitortap.data.models.User
import com.wanderer.repetitortap.data.models.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

/**
 * Repository for tutor-related Firestore queries.
 * Stubbed for now â€” will be expanded in later phases with:
 * - Tutor profile creation/editing
 * - Geolocation queries for nearby tutors
 * - Reviews and ratings
 * - Availability and bookings
 */
class TutorRepository {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    /**
     * Fetches all users with role TUTOR from Firestore.
     * In a future phase, this will be replaced with a geolocation-based query.
     */
    fun getAllTutors(): Flow<List<User>> = flow {
        val snapshot = firestore.collection("users")
            .whereEqualTo("role", UserRole.TUTOR.name)
            .get()
            .await()

        val tutors = snapshot.documents.mapNotNull { doc ->
            doc.toObject(User::class.java)
        }
        emit(tutors)
    }
}
