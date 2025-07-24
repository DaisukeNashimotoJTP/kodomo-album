package com.example.kodomo_album.data.firebase.datasource

import com.example.kodomo_album.data.firebase.models.FirebaseDiary
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
class DiaryFirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val COLLECTION_DIARIES = "diaries"
    }

    suspend fun createDiary(diary: FirebaseDiary): Result<FirebaseDiary> = try {
        val docRef = if (diary.id.isEmpty()) {
            firestore.collection(COLLECTION_DIARIES).document()
        } else {
            firestore.collection(COLLECTION_DIARIES).document(diary.id)
        }
        
        val diaryWithId = diary.copy(id = docRef.id)
        docRef.set(diaryWithId).await()
        Result.success(diaryWithId)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getDiary(diaryId: String): Result<FirebaseDiary?> = try {
        val snapshot = firestore.collection(COLLECTION_DIARIES)
            .document(diaryId)
            .get()
            .await()
        
        val diary = snapshot.toObject(FirebaseDiary::class.java)
        Result.success(diary)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getDiariesByChildId(childId: String): Result<List<FirebaseDiary>> = try {
        val snapshot = firestore.collection(COLLECTION_DIARIES)
            .whereEqualTo("childId", childId)
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .await()
        
        val diaries = snapshot.toObjects(FirebaseDiary::class.java)
        Result.success(diaries)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getDiariesByChildIdFlow(childId: String): Flow<List<FirebaseDiary>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_DIARIES)
            .whereEqualTo("childId", childId)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val diaries = snapshot?.toObjects(FirebaseDiary::class.java) ?: emptyList()
                trySend(diaries)
            }
        
        awaitClose { listener.remove() }
    }

    suspend fun getDiariesByDateRange(
        childId: String,
        startDate: Timestamp,
        endDate: Timestamp
    ): Result<List<FirebaseDiary>> = try {
        val snapshot = firestore.collection(COLLECTION_DIARIES)
            .whereEqualTo("childId", childId)
            .whereGreaterThanOrEqualTo("date", startDate)
            .whereLessThanOrEqualTo("date", endDate)
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .await()
        
        val diaries = snapshot.toObjects(FirebaseDiary::class.java)
        Result.success(diaries)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun searchDiaries(childId: String, keyword: String): Result<List<FirebaseDiary>> = try {
        val snapshot = firestore.collection(COLLECTION_DIARIES)
            .whereEqualTo("childId", childId)
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .await()
        
        val allDiaries = snapshot.toObjects(FirebaseDiary::class.java)
        val filteredDiaries = allDiaries.filter { diary ->
            diary.title.contains(keyword, ignoreCase = true) ||
            diary.content.contains(keyword, ignoreCase = true)
        }
        
        Result.success(filteredDiaries)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateDiary(diary: FirebaseDiary): Result<Unit> = try {
        firestore.collection(COLLECTION_DIARIES)
            .document(diary.id)
            .set(diary)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteDiary(diaryId: String): Result<Unit> = try {
        firestore.collection(COLLECTION_DIARIES)
            .document(diaryId)
            .delete()
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteDiariesByChildId(childId: String): Result<Unit> = try {
        val batch = firestore.batch()
        val snapshot = firestore.collection(COLLECTION_DIARIES)
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