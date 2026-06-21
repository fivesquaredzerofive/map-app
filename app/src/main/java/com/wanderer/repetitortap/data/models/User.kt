package com.wanderer.repetitortap.data.models

enum class UserRole { TUTOR, STUDENT }

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val role: UserRole = UserRole.STUDENT,
    val createdAt: Long = System.currentTimeMillis()
)
