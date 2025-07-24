package com.example.kodomo_album.data.local.dao

import androidx.room.*
import com.example.kodomo_album.data.local.entity.EventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    
    @Query("SELECT * FROM events WHERE childId = :childId ORDER BY eventDate DESC")
    fun getEventsByChildId(childId: String): Flow<List<EventEntity>>
    
    @Query("SELECT * FROM events WHERE childId = :childId AND eventDate BETWEEN :startDate AND :endDate ORDER BY eventDate ASC")
    fun getEventsByDateRange(childId: String, startDate: Long, endDate: Long): Flow<List<EventEntity>>
    
    @Query("SELECT * FROM events WHERE childId = :childId AND eventType = :eventType ORDER BY eventDate DESC")
    fun getEventsByType(childId: String, eventType: String): Flow<List<EventEntity>>
    
    @Query("SELECT * FROM events WHERE id = :eventId")
    suspend fun getEventById(eventId: String): EventEntity?
    
    @Query("SELECT * FROM events WHERE isSynced = 0")
    suspend fun getUnsyncedEvents(): List<EventEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>)
    
    @Update
    suspend fun updateEvent(event: EventEntity)
    
    @Delete
    suspend fun deleteEvent(event: EventEntity)
    
    @Query("DELETE FROM events WHERE id = :eventId")
    suspend fun deleteEventById(eventId: String)
    
    @Query("DELETE FROM events WHERE childId = :childId")
    suspend fun deleteEventsByChildId(childId: String)
    
    @Query("UPDATE events SET isSynced = 1 WHERE id = :eventId")
    suspend fun markAsSynced(eventId: String)
}