package com.example.kodomo_album.data.firebase.datasource

import com.example.kodomo_album.data.firebase.models.FirebaseChild
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChildFirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val COLLECTION_CHILDREN = "children"
    }

    suspend fun createChild(child: FirebaseChild): Result<FirebaseChild> = try {
        val docRef = if (child.id.isEmpty()) {
            firestore.collection(COLLECTION_CHILDREN).document()
        } else {
            firestore.collection(COLLECTION_CHILDREN).document(child.id)
        }
        
        val childWithId = child.copy(id = docRef.id)
        docRef.set(childWithId).await()
        Result.success(childWithId)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getChild(childId: String): Result<FirebaseChild?> = try {
        val snapshot = firestore.collection(COLLECTION_CHILDREN)
            .document(childId)
            .get()
            .await()
        
        val child = snapshot.toObject(FirebaseChild::class.java)
        Result.success(child)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getChildrenByUserId(userId: String): Result<List<FirebaseChild>> = try {
        val snapshot = firestore.collection(COLLECTION_CHILDREN)
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
        
        val children = snapshot.toObjects(FirebaseChild::class.java)
        Result.success(children)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getChildrenByUserIdFlow(userId: String): Flow<List<FirebaseChild>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_CHILDREN)
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val children = snapshot?.toObjects(FirebaseChild::class.java) ?: emptyList()
                trySend(children)
            }
        
        awaitClose { listener.remove() }
    }

    suspend fun updateChild(child: FirebaseChild): Result<Unit> = try {
        firestore.collection(COLLECTION_CHILDREN)
            .document(child.id)
            .set(child)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteChild(childId: String): Result<Unit> = try {
        firestore.collection(COLLECTION_CHILDREN)
            .document(childId)
            .delete()
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}