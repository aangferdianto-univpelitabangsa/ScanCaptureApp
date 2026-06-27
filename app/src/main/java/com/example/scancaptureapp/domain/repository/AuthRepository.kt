package com.example.scancaptureapp.domain.repository

import com.example.scancaptureapp.domain.model.User

interface AuthRepository {
    val currentUser: User?
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(name: String, email: String, password: String): Result<User>
    fun logout()
}
