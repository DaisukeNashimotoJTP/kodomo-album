package com.example.kodomo_album.data.local.dao

import androidx.room.*
import com.example.kodomo_album.data.local.entity.MilestoneEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MilestoneDao {
    
    @Query("SELECT * FROM milestones WHERE childId = :childId ORDER BY achievedAt DESC")
    fun getMilestonesByChildId(childId: String): Flow<List<MilestoneEntity>>
    
    @Query("SELECT * FROM milestones WHERE childId = :childId AND type = :type ORDER BY achievedAt DESC")
    fun getMilestonesByType(childId: String, type: String): Flow<List<MilestoneEntity>>
    
    @Query("SELECT * FROM milestones WHERE id = :milestoneId")
    suspend fun getMilestoneById(milestoneId: String): MilestoneEntity?
    
    @Query("SELECT * FROM milestones WHERE isSynced = 0")
    suspend fun getUnsyncedMilestones(): List<MilestoneEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestone(milestone: MilestoneEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestones(milestones: List<MilestoneEntity>)
    
    @Update
    suspend fun updateMilestone(milestone: MilestoneEntity)
    
    @Delete
    suspend fun deleteMilestone(milestone: MilestoneEntity)
    
    @Query("DELETE FROM milestones WHERE id = :milestoneId")
    suspend fun deleteMilestoneById(milestoneId: String)
    
    @Query("DELETE FROM milestones WHERE childId = :childId")
    suspend fun deleteMilestonesByChildId(childId: String)
    
    @Query("UPDATE milestones SET isSynced = 1 WHERE id = :milestoneId")
    suspend fun markAsSynced(milestoneId: String)
}