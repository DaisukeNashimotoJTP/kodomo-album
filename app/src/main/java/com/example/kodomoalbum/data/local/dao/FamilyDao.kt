package com.example.kodomoalbum.data.local.dao

import androidx.room.*
import com.example.kodomoalbum.data.local.entity.FamilyEntity
import com.example.kodomoalbum.data.local.entity.InvitationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyDao {
    
    @Query("SELECT * FROM families WHERE id = :familyId")
    suspend fun getFamilyById(familyId: String): FamilyEntity?
    
    @Query("SELECT * FROM families WHERE createdBy = :userId")
    suspend fun getFamiliesByUserId(userId: String): List<FamilyEntity>
    
    @Query("SELECT * FROM families WHERE members LIKE '%' || :userId || '%'")
    fun getFamiliesForUser(userId: String): Flow<List<FamilyEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamily(family: FamilyEntity)
    
    @Update
    suspend fun updateFamily(family: FamilyEntity)
    
    @Delete
    suspend fun deleteFamily(family: FamilyEntity)
    
    @Query("SELECT * FROM invitations WHERE id = :invitationId")
    suspend fun getInvitationById(invitationId: String): InvitationEntity?
    
    @Query("SELECT * FROM invitations WHERE inviteeEmail = :email AND status = 'PENDING'")
    suspend fun getPendingInvitationsForEmail(email: String): List<InvitationEntity>
    
    @Query("SELECT * FROM invitations WHERE familyId = :familyId")
    suspend fun getInvitationsForFamily(familyId: String): List<InvitationEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvitation(invitation: InvitationEntity)
    
    @Update
    suspend fun updateInvitation(invitation: InvitationEntity)
    
    @Delete
    suspend fun deleteInvitation(invitation: InvitationEntity)
}