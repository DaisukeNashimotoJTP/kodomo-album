package com.example.kodomo_album.data.firebase.datasource

import com.example.kodomo_album.data.firebase.models.FirebaseGrowthRecord
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
class GrowthRecordFirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val COLLECTION_GROWTH_RECORDS = "growth_records"
    }

    suspend fun createGrowthRecord(record: FirebaseGrowthRecord): Result<FirebaseGrowthRecord> = try {
        val docRef = if (record.id.isEmpty()) {
            firestore.collection(COLLECTION_GROWTH_RECORDS).document()
        } else {
            firestore.collection(COLLECTION_GROWTH_RECORDS).document(record.id)
        }
        
        val recordWithId = record.copy(id = docRef.id)
        docRef.set(recordWithId).await()
        Result.success(recordWithId)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getGrowthRecord(recordId: String): Result<FirebaseGrowthRecord?> = try {
        val snapshot = firestore.collection(COLLECTION_GROWTH_RECORDS)
            .document(recordId)
            .get()
            .await()
        
        val record = snapshot.toObject(FirebaseGrowthRecord::class.java)
        Result.success(record)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getGrowthRecordsByChildId(childId: String): Result<List<FirebaseGrowthRecord>> = try {
        val snapshot = firestore.collection(COLLECTION_GROWTH_RECORDS)
            .whereEqualTo("childId", childId)
            .orderBy("recordedAt", Query.Direction.ASCENDING)
            .get()
            .await()
        
        val records = snapshot.toObjects(FirebaseGrowthRecord::class.java)
        Result.success(records)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getGrowthRecordsByChildIdFlow(childId: String): Flow<List<FirebaseGrowthRecord>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_GROWTH_RECORDS)
            .whereEqualTo("childId", childId)
            .orderBy("recordedAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val records = snapshot?.toObjects(FirebaseGrowthRecord::class.java) ?: emptyList()
                trySend(records)
            }
        
        awaitClose { listener.remove() }
    }

    suspend fun getGrowthRecordsByDateRange(
        childId: String,
        startDate: Timestamp,
        endDate: Timestamp
    ): Result<List<FirebaseGrowthRecord>> = try {
        val snapshot = firestore.collection(COLLECTION_GROWTH_RECORDS)
            .whereEqualTo("childId", childId)
            .whereGreaterThanOrEqualTo("recordedAt", startDate)
            .whereLessThanOrEqualTo("recordedAt", endDate)
            .orderBy("recordedAt", Query.Direction.ASCENDING)
            .get()
            .await()
        
        val records = snapshot.toObjects(FirebaseGrowthRecord::class.java)
        Result.success(records)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateGrowthRecord(record: FirebaseGrowthRecord): Result<Unit> = try {
        firestore.collection(COLLECTION_GROWTH_RECORDS)
            .document(record.id)
            .set(record)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteGrowthRecord(recordId: String): Result<Unit> = try {
        firestore.collection(COLLECTION_GROWTH_RECORDS)
            .document(recordId)
            .delete()
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteGrowthRecordsByChildId(childId: String): Result<Unit> = try {
        val batch = firestore.batch()
        val snapshot = firestore.collection(COLLECTION_GROWTH_RECORDS)
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