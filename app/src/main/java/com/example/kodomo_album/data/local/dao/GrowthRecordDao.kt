package com.example.kodomo_album.data.local.dao

import androidx.room.*
import com.example.kodomo_album.data.local.entity.GrowthRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GrowthRecordDao {
    
    @Query("SELECT * FROM growth_records WHERE childId = :childId ORDER BY recordedAt ASC")
    fun getGrowthRecordsByChildId(childId: String): Flow<List<GrowthRecordEntity>>
    
    @Query("SELECT * FROM growth_records WHERE childId = :childId AND recordedAt BETWEEN :startDate AND :endDate ORDER BY recordedAt ASC")
    fun getGrowthRecordsByDateRange(childId: String, startDate: Long, endDate: Long): Flow<List<GrowthRecordEntity>>
    
    @Query("SELECT * FROM growth_records WHERE id = :recordId")
    suspend fun getGrowthRecordById(recordId: String): GrowthRecordEntity?
    
    @Query("SELECT * FROM growth_records WHERE childId = :childId ORDER BY recordedAt DESC LIMIT 1")
    suspend fun getLatestGrowthRecord(childId: String): GrowthRecordEntity?
    
    @Query("SELECT * FROM growth_records WHERE childId = :childId AND height IS NOT NULL ORDER BY recordedAt ASC")
    suspend fun getHeightRecords(childId: String): List<GrowthRecordEntity>
    
    @Query("SELECT * FROM growth_records WHERE childId = :childId AND weight IS NOT NULL ORDER BY recordedAt ASC")
    suspend fun getWeightRecords(childId: String): List<GrowthRecordEntity>
    
    @Query("SELECT * FROM growth_records WHERE isSynced = 0")
    suspend fun getUnsyncedRecords(): List<GrowthRecordEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrowthRecord(record: GrowthRecordEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrowthRecords(records: List<GrowthRecordEntity>)
    
    @Update
    suspend fun updateGrowthRecord(record: GrowthRecordEntity)
    
    @Delete
    suspend fun deleteGrowthRecord(record: GrowthRecordEntity)
    
    @Query("DELETE FROM growth_records WHERE id = :recordId")
    suspend fun deleteGrowthRecordById(recordId: String)
    
    @Query("DELETE FROM growth_records WHERE childId = :childId")
    suspend fun deleteGrowthRecordsByChildId(childId: String)
    
    @Query("UPDATE growth_records SET isSynced = 1 WHERE id = :recordId")
    suspend fun markAsSynced(recordId: String)
}