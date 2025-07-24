package com.example.kodomo_album.data.local.dao

import androidx.room.*
import com.example.kodomo_album.data.local.entity.DiaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {
    
    @Query("SELECT * FROM diaries WHERE childId = :childId ORDER BY date DESC")
    fun getDiariesByChildIdFlow(childId: String): Flow<List<DiaryEntity>>
    
    @Query("SELECT * FROM diaries WHERE childId = :childId ORDER BY date DESC")
    suspend fun getDiariesByChildId(childId: String): List<DiaryEntity>
    
    @Query("SELECT * FROM diaries WHERE childId = :childId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getDiariesByDateRange(childId: String, startDate: Long, endDate: Long): Flow<List<DiaryEntity>>
    
    @Query("SELECT * FROM diaries WHERE id = :diaryId")
    suspend fun getDiaryById(diaryId: String): DiaryEntity?
    
    @Query("SELECT * FROM diaries WHERE childId = :childId AND (title LIKE '%' || :keyword || '%' OR content LIKE '%' || :keyword || '%') ORDER BY date DESC")
    suspend fun searchDiaries(childId: String, keyword: String): List<DiaryEntity>
    
    @Query("SELECT * FROM diaries WHERE isSynced = 0")
    suspend fun getUnsyncedDiaries(): List<DiaryEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiary(diary: DiaryEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiaries(diaries: List<DiaryEntity>)
    
    @Update
    suspend fun update(diary: DiaryEntity)
    
    @Delete
    suspend fun deleteDiary(diary: DiaryEntity)
    
    @Query("DELETE FROM diaries WHERE id = :diaryId")
    suspend fun deleteById(diaryId: String)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(diary: DiaryEntity)
    
    @Query("DELETE FROM diaries WHERE childId = :childId")
    suspend fun deleteDiariesByChildId(childId: String)
    
    @Query("UPDATE diaries SET isSynced = 1 WHERE id = :diaryId")
    suspend fun markAsSynced(diaryId: String)
}