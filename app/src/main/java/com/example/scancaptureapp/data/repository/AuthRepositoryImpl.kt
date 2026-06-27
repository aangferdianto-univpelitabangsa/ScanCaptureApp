package com.example.scancaptureapp.data.repository

import com.example.scancaptureapp.data.remote.FirebaseAuthDataSource
import com.example.scancaptureapp.domain.model.User
import com.example.scancaptureapp.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuthDataSource: FirebaseAuthDataSource
) : AuthRepository {

    override val currentUser: User?
        get() = firebaseAuthDataSource.currentFirebaseUser?.let {
            firebaseAuthDataSource.toUser(it)
        }

    override suspend fun login(email: String, password: String): Result<User> =
        firebaseAuthDataSource.login(email, password)

    override suspend fun register(name: String, email: String, password: String): Result<User> =
        firebaseAuthDataSource.register(name, email, password)

    override fun logout() {
        firebaseAuthDataSource.logout()
    }
}
