package com.example.kodomo_album.data.repository

import com.example.kodomo_album.core.util.Constants
import com.example.kodomo_album.core.util.ErrorHandler
import com.example.kodomo_album.core.util.Resource
import com.example.kodomo_album.data.local.dao.UserDao
import com.example.kodomo_album.data.local.entity.UserEntity
import com.example.kodomo_album.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao
) : AuthRepository, BaseRepository() {

    override suspend fun signInWithEmailAndPassword(email: String, password: String): Resource<User> {
        return safeApiCall {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: throw Exception("ユーザー情報の取得に失敗しました")
            
            val user = getUserFromFirestore(firebaseUser.uid)
            saveUserToLocal(user)
            user
        }
    }

    override suspend fun signUpWithEmailAndPassword(
        email: String, 
        password: String, 
        displayName: String
    ): Resource<User> {
        return safeApiCall {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: throw Exception("ユーザーの作成に失敗しました")

            // Update profile
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            firebaseUser.updateProfile(profileUpdates).await()

            // Create user document in Firestore
            val user = User(
                id = firebaseUser.uid,
                email = email,
                displayName = displayName,
                profileImageUrl = "",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            saveUserToFirestore(user)
            saveUserToLocal(user)
            user
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Resource<Unit> {
        return safeApiCall {
            firebaseAuth.sendPasswordResetEmail(email).await()
        }
    }

    override suspend fun signOut(): Resource<Unit> {
        return safeApiCall {
            firebaseAuth.signOut()
            userDao.deleteAll()
        }
    }

    override fun getCurrentUser(): Flow<User?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                // Launch coroutine for suspend functions
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val localUser = userDao.getUserById(firebaseUser.uid)?.let { userEntity ->
                            User(
                                id = userEntity.id,
                                email = userEntity.email,
                                displayName = userEntity.displayName,
                                profileImageUrl = userEntity.profileImageUrl,
                                createdAt = userEntity.createdAt,
                                updatedAt = userEntity.updatedAt,
                                familyId = userEntity.familyId,
                                isPartner = userEntity.isPartner
                            )
                        }

                        if (localUser != null) {
                            trySend(localUser)
                        } else {
                            // Fetch from Firestore if not in local database
                            try {
                                val user = getUserFromFirestore(firebaseUser.uid)
                                saveUserToLocal(user)
                                trySend(user)
                            } catch (e: Exception) {
                                // If Firestore user doesn't exist, create basic user from Firebase Auth
                                val basicUser = User(
                                    id = firebaseUser.uid,
                                    email = firebaseUser.email ?: "",
                                    displayName = firebaseUser.displayName ?: "",
                                    profileImageUrl = firebaseUser.photoUrl?.toString() ?: "",
                                    createdAt = System.currentTimeMillis(),
                                    updatedAt = System.currentTimeMillis()
                                )
                                try {
                                    saveUserToFirestore(basicUser)
                                    saveUserToLocal(basicUser)
                                    trySend(basicUser)
                                } catch (saveException: Exception) {
                                    trySend(null)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        trySend(null)
                    }
                }
            } else {
                trySend(null)
            }
        }

        firebaseAuth.addAuthStateListener(authStateListener)
        awaitClose { firebaseAuth.removeAuthStateListener(authStateListener) }
    }

    override suspend fun updateUserProfile(user: User): Resource<User> {
        return safeApiCall {
            val updatedUser = user.copy(updatedAt = System.currentTimeMillis())
            saveUserToFirestore(updatedUser)
            saveUserToLocal(updatedUser)
            updatedUser
        }
    }

    private suspend fun getUserFromFirestore(userId: String): User {
        val document = firestore.collection(Constants.USERS_COLLECTION)
            .document(userId)
            .get()
            .await()

        return if (document.exists()) {
            document.toObject(User::class.java) ?: throw Exception("ユーザー情報の変換に失敗しました")
        } else {
            throw Exception("ユーザー情報が見つかりません")
        }
    }

    private suspend fun saveUserToFirestore(user: User) {
        firestore.collection(Constants.USERS_COLLECTION)
            .document(user.id)
            .set(user)
            .await()
    }

    private suspend fun saveUserToLocal(user: User) {
        val userEntity = UserEntity(
            id = user.id,
            email = user.email,
            displayName = user.displayName,
            profileImageUrl = user.profileImageUrl,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt,
            familyId = user.familyId,
            isPartner = user.isPartner
        )
        userDao.insertUser(userEntity)
    }
}