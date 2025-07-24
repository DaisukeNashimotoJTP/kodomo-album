package com.example.kodomo_album.data.repository

import com.example.kodomo_album.core.util.Resource
import com.example.kodomo_album.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signInWithEmailAndPassword(email: String, password: String): Resource<User>
    suspend fun signUpWithEmailAndPassword(email: String, password: String, displayName: String): Resource<User>
    suspend fun sendPasswordResetEmail(email: String): Resource<Unit>
    suspend fun signOut(): Resource<Unit>
    fun getCurrentUser(): Flow<User?>
    suspend fun updateUserProfile(user: User): Resource<User>
}