package com.example.kodomo_album.data.local.dao

import androidx.room.*
import com.example.kodomo_album.data.local.entity.ChildEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChildDao {
    
    @Query("SELECT * FROM children WHERE userId = :userId ORDER BY birthDate DESC")
    fun getChildrenByUserIdFlow(userId: String): Flow<List<ChildEntity>>
    
    @Query("SELECT * FROM children WHERE userId = :userId ORDER BY birthDate DESC")
    suspend fun getChildrenByUserId(userId: String): List<ChildEntity>
    
    @Query("SELECT * FROM children WHERE id = :childId")
    suspend fun getChildById(childId: String): ChildEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChild(child: ChildEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChildren(children: List<ChildEntity>)
    
    @Update
    suspend fun update(child: ChildEntity)
    
    @Delete
    suspend fun deleteChild(child: ChildEntity)
    
    @Query("DELETE FROM children WHERE id = :childId")
    suspend fun deleteById(childId: String)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(child: ChildEntity)
    
    @Query("DELETE FROM children WHERE userId = :userId")
    suspend fun deleteChildrenByUserId(userId: String)
}