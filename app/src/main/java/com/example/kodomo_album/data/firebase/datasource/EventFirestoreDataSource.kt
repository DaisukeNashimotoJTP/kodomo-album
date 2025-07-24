package com.example.kodomo_album.data.firebase.datasource

import com.example.kodomo_album.data.firebase.models.FirebaseEvent
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
class EventFirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val COLLECTION_EVENTS = "events"
    }

    suspend fun createEvent(event: FirebaseEvent): Result<FirebaseEvent> = try {
        val docRef = if (event.id.isEmpty()) {
            firestore.collection(COLLECTION_EVENTS).document()
        } else {
            firestore.collection(COLLECTION_EVENTS).document(event.id)
        }
        
        val eventWithId = event.copy(id = docRef.id)
        docRef.set(eventWithId).await()
        Result.success(eventWithId)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getEvent(eventId: String): Result<FirebaseEvent?> = try {
        val snapshot = firestore.collection(COLLECTION_EVENTS)
            .document(eventId)
            .get()
            .await()
        
        val event = snapshot.toObject(FirebaseEvent::class.java)
        Result.success(event)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getEventsByChildId(childId: String): Result<List<FirebaseEvent>> = try {
        val snapshot = firestore.collection(COLLECTION_EVENTS)
            .whereEqualTo("childId", childId)
            .orderBy("eventDate", Query.Direction.DESCENDING)
            .get()
            .await()
        
        val events = snapshot.toObjects(FirebaseEvent::class.java)
        Result.success(events)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getEventsByChildIdFlow(childId: String): Flow<List<FirebaseEvent>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_EVENTS)
            .whereEqualTo("childId", childId)
            .orderBy("eventDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val events = snapshot?.toObjects(FirebaseEvent::class.java) ?: emptyList()
                trySend(events)
            }
        
        awaitClose { listener.remove() }
    }

    suspend fun getEventsByType(childId: String, eventType: String): Result<List<FirebaseEvent>> = try {
        val snapshot = firestore.collection(COLLECTION_EVENTS)
            .whereEqualTo("childId", childId)
            .whereEqualTo("eventType", eventType)
            .orderBy("eventDate", Query.Direction.DESCENDING)
            .get()
            .await()
        
        val events = snapshot.toObjects(FirebaseEvent::class.java)
        Result.success(events)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getEventsByDateRange(
        childId: String,
        startDate: Timestamp,
        endDate: Timestamp
    ): Result<List<FirebaseEvent>> = try {
        val snapshot = firestore.collection(COLLECTION_EVENTS)
            .whereEqualTo("childId", childId)
            .whereGreaterThanOrEqualTo("eventDate", startDate)
            .whereLessThanOrEqualTo("eventDate", endDate)
            .orderBy("eventDate", Query.Direction.DESCENDING)
            .get()
            .await()
        
        val events = snapshot.toObjects(FirebaseEvent::class.java)
        Result.success(events)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateEvent(event: FirebaseEvent): Result<Unit> = try {
        firestore.collection(COLLECTION_EVENTS)
            .document(event.id)
            .set(event)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteEvent(eventId: String): Result<Unit> = try {
        firestore.collection(COLLECTION_EVENTS)
            .document(eventId)
            .delete()
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteEventsByChildId(childId: String): Result<Unit> = try {
        val batch = firestore.batch()
        val snapshot = firestore.collection(COLLECTION_EVENTS)
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