package com.example.kodomo_album.data.firebase.datasource

import com.example.kodomo_album.data.firebase.models.FirebaseMedia
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaFirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val COLLECTION_MEDIA = "media"
    }

    suspend fun createMedia(media: FirebaseMedia): Result<FirebaseMedia> = try {
        val docRef = if (media.id.isEmpty()) {
            firestore.collection(COLLECTION_MEDIA).document()
        } else {
            firestore.collection(COLLECTION_MEDIA).document(media.id)
        }
        
        val mediaWithId = media.copy(id = docRef.id)
        docRef.set(mediaWithId).await()
        Result.success(mediaWithId)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getMedia(mediaId: String): Result<FirebaseMedia?> = try {
        val snapshot = firestore.collection(COLLECTION_MEDIA)
            .document(mediaId)
            .get()
            .await()
        
        val media = snapshot.toObject(FirebaseMedia::class.java)
        Result.success(media)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getMediaByChildId(childId: String): Result<List<FirebaseMedia>> = try {
        val snapshot = firestore.collection(COLLECTION_MEDIA)
            .whereEqualTo("childId", childId)
            .orderBy("takenAt", Query.Direction.DESCENDING)
            .get()
            .await()
        
        val mediaList = snapshot.toObjects(FirebaseMedia::class.java)
        Result.success(mediaList)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getMediaByChildIdFlow(childId: String): Flow<List<FirebaseMedia>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_MEDIA)
            .whereEqualTo("childId", childId)
            .orderBy("takenAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val mediaList = snapshot?.toObjects(FirebaseMedia::class.java) ?: emptyList()
                trySend(mediaList)
            }
        
        awaitClose { listener.remove() }
    }

    suspend fun getMediaByType(childId: String, type: String): Result<List<FirebaseMedia>> = try {
        val snapshot = firestore.collection(COLLECTION_MEDIA)
            .whereEqualTo("childId", childId)
            .whereEqualTo("type", type)
            .orderBy("takenAt", Query.Direction.DESCENDING)
            .get()
            .await()
        
        val mediaList = snapshot.toObjects(FirebaseMedia::class.java)
        Result.success(mediaList)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getMediaByDateRange(
        childId: String,
        startDate: Timestamp,
        endDate: Timestamp
    ): Result<List<FirebaseMedia>> = try {
        val snapshot = firestore.collection(COLLECTION_MEDIA)
            .whereEqualTo("childId", childId)
            .whereGreaterThanOrEqualTo("takenAt", startDate)
            .whereLessThanOrEqualTo("takenAt", endDate)
            .orderBy("takenAt", Query.Direction.DESCENDING)
            .get()
            .await()
        
        val mediaList = snapshot.toObjects(FirebaseMedia::class.java)
        Result.success(mediaList)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateMedia(media: FirebaseMedia): Result<Unit> = try {
        firestore.collection(COLLECTION_MEDIA)
            .document(media.id)
            .set(media)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteMedia(mediaId: String): Result<Unit> = try {
        firestore.collection(COLLECTION_MEDIA)
            .document(mediaId)
            .delete()
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteMediaByChildId(childId: String): Result<Unit> = try {
        val batch = firestore.batch()
        val snapshot = firestore.collection(COLLECTION_MEDIA)
            .whereEqualTo("childId", childId)
            .get()
            .await()
        
        snapshot.documents.forEach { document ->
            batch.delete(document.reference)
        }
        
        batch.commit().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}