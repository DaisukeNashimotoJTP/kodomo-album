package com.example.kodomo_album.data.local.dao

import androidx.room.*
import com.example.kodomo_album.data.local.entity.ChildEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChildDao {
    
    @Query("SELECT * FROM children WHERE userId = :userId ORDER BY birthDate DESC")
    fun getChildrenByUserId(userId: String): Flow<List<ChildEntity>>
    
    @Query("SELECT * FROM children WHERE id = :childId")
    suspend fun getChildById(childId: String): ChildEntity?
    
    @Query("SELECT * FROM children WHERE userId = :userId ORDER BY birthDate DESC")
    suspend fun getChildrenByUserIdSync(userId: String): List<ChildEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChild(child: ChildEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChildren(children: List<ChildEntity>)
    
    @Update
    suspend fun updateChild(child: ChildEntity)
    
    @Delete
    suspend fun deleteChild(child: ChildEntity)
    
    @Query("DELETE FROM children WHERE id = :childId")
    suspend fun deleteChildById(childId: String)
    
    @Query("DELETE FROM children WHERE userId = :userId")
    suspend fun deleteChildrenByUserId(userId: String)
}