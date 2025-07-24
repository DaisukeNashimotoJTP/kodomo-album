package com.example.kodomo_album.data.firebase.datasource

import com.example.kodomo_album.data.firebase.models.FirebaseMilestone
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
class MilestoneFirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val COLLECTION_MILESTONES = "milestones"
    }

    suspend fun createMilestone(milestone: FirebaseMilestone): Result<FirebaseMilestone> = try {
        val docRef = if (milestone.id.isEmpty()) {
            firestore.collection(COLLECTION_MILESTONES).document()
        } else {
            firestore.collection(COLLECTION_MILESTONES).document(milestone.id)
        }
        
        val milestoneWithId = milestone.copy(id = docRef.id)
        docRef.set(milestoneWithId).await()
        Result.success(milestoneWithId)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getMilestone(milestoneId: String): Result<FirebaseMilestone?> = try {
        val snapshot = firestore.collection(COLLECTION_MILESTONES)
            .document(milestoneId)
            .get()
            .await()
        
        val milestone = snapshot.toObject(FirebaseMilestone::class.java)
        Result.success(milestone)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getMilestonesByChildId(childId: String): Result<List<FirebaseMilestone>> = try {
        val snapshot = firestore.collection(COLLECTION_MILESTONES)
            .whereEqualTo("childId", childId)
            .orderBy("achievedAt", Query.Direction.DESCENDING)
            .get()
            .await()
        
        val milestones = snapshot.toObjects(FirebaseMilestone::class.java)
        Result.success(milestones)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getMilestonesByChildIdFlow(childId: String): Flow<List<FirebaseMilestone>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_MILESTONES)
            .whereEqualTo("childId", childId)
            .orderBy("achievedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val milestones = snapshot?.toObjects(FirebaseMilestone::class.java) ?: emptyList()
                trySend(milestones)
            }
        
        awaitClose { listener.remove() }
    }

    suspend fun getMilestonesByType(childId: String, type: String): Result<List<FirebaseMilestone>> = try {
        val snapshot = firestore.collection(COLLECTION_MILESTONES)
            .whereEqualTo("childId", childId)
            .whereEqualTo("type", type)
            .orderBy("achievedAt", Query.Direction.DESCENDING)
            .get()
            .await()
        
        val milestones = snapshot.toObjects(FirebaseMilestone::class.java)
        Result.success(milestones)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getMilestonesByDateRange(
        childId: String,
        startDate: Timestamp,
        endDate: Timestamp
    ): Result<List<FirebaseMilestone>> = try {
        val snapshot = firestore.collection(COLLECTION_MILESTONES)
            .whereEqualTo("childId", childId)
            .whereGreaterThanOrEqualTo("achievedAt", startDate)
            .whereLessThanOrEqualTo("achievedAt", endDate)
            .orderBy("achievedAt", Query.Direction.DESCENDING)
            .get()
            .await()
        
        val milestones = snapshot.toObjects(FirebaseMilestone::class.java)
        Result.success(milestones)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateMilestone(milestone: FirebaseMilestone): Result<Unit> = try {
        firestore.collection(COLLECTION_MILESTONES)
            .document(milestone.id)
            .set(milestone)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteMilestone(milestoneId: String): Result<Unit> = try {
        firestore.collection(COLLECTION_MILESTONES)
            .document(milestoneId)
            .delete()
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteMilestonesByChildId(childId: String): Result<Unit> = try {
        val batch = firestore.batch()
        val snapshot = firestore.collection(COLLECTION_MILESTONES)
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