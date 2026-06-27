package com.example.scancaptureapp.data.remote

import android.content.Context
import com.example.scancaptureapp.R
import com.example.scancaptureapp.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthDataSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    @ApplicationContext private val context: Context
) {

    val currentFirebaseUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    fun toUser(firebaseUser: FirebaseUser): User = User(
        uid = firebaseUser.uid,
        name = firebaseUser.displayName.orEmpty().ifBlank {
            context.getString(R.string.user_default_name)
        },
        email = firebaseUser.email.orEmpty()
    )

    suspend fun login(email: String, password: String): Result<User> = runCatching {
        val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        val user = result.user ?: throw IllegalStateException("Login failed: user is null")
        toUser(user)
    }

    suspend fun register(name: String, email: String, password: String): Result<User> = runCatching {
        val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        val user = result.user ?: throw IllegalStateException("Registration failed: user is null")
        val profileUpdate = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()
        user.updateProfile(profileUpdate).await()
        user.reload().await()
        toUser(firebaseAuth.currentUser ?: user)
    }

    fun logout() {
        firebaseAuth.signOut()
    }
}
