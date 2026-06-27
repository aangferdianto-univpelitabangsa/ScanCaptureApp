package com.example.scancaptureapp.domain.model

/**
 * Domain representation of an authenticated user.
 */
data class User(
    val uid: String,
    val name: String,
    val email: String
)
