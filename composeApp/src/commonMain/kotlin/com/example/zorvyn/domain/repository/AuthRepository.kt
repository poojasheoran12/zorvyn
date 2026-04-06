package com.example.zorvyn.domain.repository

import dev.gitlive.firebase.auth.FirebaseUser

interface AuthRepository {
    val currentUser: FirebaseUser?
    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser?>
    suspend fun signInAnonymously(): Result<FirebaseUser?>
    suspend fun signOut()
    fun isUserLoggedIn(): Boolean
}
