package com.example.kodomo_album.data.local.dao

import androidx.room.*
import com.example.kodomo_album.data.local.entity.MediaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    
    @Query("SELECT * FROM media WHERE childId = :childId ORDER BY takenAt DESC")
    fun getMediaByChildId(childId: String): Flow<List<MediaEntity>>
    
    @Query("SELECT * FROM media WHERE childId = :childId AND takenAt BETWEEN :startDate AND :endDate ORDER BY takenAt DESC")
    fun getMediaByDateRange(childId: String, startDate: Long, endDate: Long): Flow<List<MediaEntity>>
    
    @Query("SELECT * FROM media WHERE id = :mediaId")
    suspend fun getMediaById(mediaId: String): MediaEntity?
    
    @Query("SELECT * FROM media WHERE type = :type AND childId = :childId ORDER BY takenAt DESC")
    suspend fun getMediaByType(childId: String, type: String): List<MediaEntity>
    
    @Query("SELECT * FROM media WHERE isUploaded = 0")
    suspend fun getUnuploadedMedia(): List<MediaEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: MediaEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaList(mediaList: List<MediaEntity>)
    
    @Update
    suspend fun updateMedia(media: MediaEntity)
    
    @Delete
    suspend fun deleteMedia(media: MediaEntity)
    
    @Query("DELETE FROM media WHERE id = :mediaId")
    suspend fun deleteMediaById(mediaId: String)
    
    @Query("DELETE FROM media WHERE childId = :childId")
    suspend fun deleteMediaByChildId(childId: String)
    
    @Query("UPDATE media SET isUploaded = 1, url = :url WHERE id = :mediaId")
    suspend fun markAsUploaded(mediaId: String, url: String)
}