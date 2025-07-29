package com.example.kodomoalbum.data.repository

import com.example.kodomoalbum.data.local.dao.FamilyDao
import com.example.kodomoalbum.data.local.entity.FamilyEntity
import com.example.kodomoalbum.data.local.entity.InvitationEntity
import com.example.kodomoalbum.data.remote.firebase.FirebaseFamilyDataSource
import com.example.kodomoalbum.domain.model.*
import com.example.kodomo_album.data.repository.AuthRepository
import com.example.kodomoalbum.domain.repository.SharingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharingRepositoryImpl @Inject constructor(
    private val familyDao: FamilyDao,
    private val firebaseFamilyDataSource: FirebaseFamilyDataSource,
    private val authRepository: AuthRepository
) : SharingRepository {
    
    override suspend fun invitePartner(email: String, familyName: String): Result<Invitation> {
        return try {
            val currentUser = authRepository.getCurrentUser().first()
                ?: return Result.failure(Exception("User not authenticated"))
                
            // 家族が存在しない場合は新規作成
            val family = getOrCreateFamily(familyName, currentUser.id)
            
            val invitation = Invitation(
                id = UUID.randomUUID().toString(),
                familyId = family.id,
                inviterUserId = currentUser.id,
                inviterName = currentUser.name,
                inviteeEmail = email,
                status = InvitationStatus.PENDING,
                createdAt = LocalDateTime.now(),
                expiresAt = LocalDateTime.now().plusDays(7) // 7日間有効
            )
            
            // ローカルDBに保存
            familyDao.insertInvitation(invitation.toEntity())
            
            // Firestoreに保存
            firebaseFamilyDataSource.createInvitation(invitation)
            
            // 招待メール送信
            firebaseFamilyDataSource.sendInvitationEmail(invitation)
            
            Result.success(invitation)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun acceptInvitation(invitationId: String): Result<Unit> {
        return try {
            val currentUser = authRepository.getCurrentUser().first()
                ?: return Result.failure(Exception("User not authenticated"))
                
            val invitation = familyDao.getInvitationById(invitationId)
                ?: return Result.failure(Exception("Invitation not found"))
                
            if (invitation.status != InvitationStatus.PENDING.name) {
                return Result.failure(Exception("Invitation is not pending"))
            }
            
            if (invitation.expiresAt.isBefore(LocalDateTime.now())) {
                return Result.failure(Exception("Invitation has expired"))
            }
            
            // 家族に追加
            val family = familyDao.getFamilyById(invitation.familyId)
                ?: return Result.failure(Exception("Family not found"))
                
            val newMember = FamilyMember(
                userId = currentUser.id,
                email = currentUser.email,
                name = currentUser.name,
                role = FamilyRole.MEMBER,
                joinedAt = LocalDateTime.now()
            )
            
            val updatedFamily = family.toDomain().copy(
                members = family.toDomain().members + newMember,
                updatedAt = LocalDateTime.now()
            )
            
            // 招待ステータスを更新
            val updatedInvitation = invitation.copy(status = InvitationStatus.ACCEPTED.name)
            
            // データベース更新
            familyDao.updateFamily(updatedFamily.toEntity())
            familyDao.updateInvitation(updatedInvitation)
            
            // Firestore更新
            firebaseFamilyDataSource.updateFamily(updatedFamily)
            firebaseFamilyDataSource.updateInvitation(updatedInvitation.toDomain())
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun declineInvitation(invitationId: String): Result<Unit> {
        return try {
            val invitation = familyDao.getInvitationById(invitationId)
                ?: return Result.failure(Exception("Invitation not found"))
                
            val updatedInvitation = invitation.copy(status = InvitationStatus.DECLINED.name)
            
            familyDao.updateInvitation(updatedInvitation)
            firebaseFamilyDataSource.updateInvitation(updatedInvitation.toDomain())
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun createFamily(name: String): Result<Family> {
        return try {
            val currentUser = authRepository.getCurrentUser().first()
                ?: return Result.failure(Exception("User not authenticated"))
                
            val family = Family(
                id = UUID.randomUUID().toString(),
                name = name,
                createdBy = currentUser.id,
                members = listOf(
                    FamilyMember(
                        userId = currentUser.id,
                        email = currentUser.email,
                        name = currentUser.name,
                        role = FamilyRole.ADMIN,
                        joinedAt = LocalDateTime.now()
                    )
                ),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            
            familyDao.insertFamily(family.toEntity())
            firebaseFamilyDataSource.createFamily(family)
            
            Result.success(family)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getFamily(familyId: String): Result<Family> {
        return try {
            val family = familyDao.getFamilyById(familyId)?.toDomain()
                ?: return Result.failure(Exception("Family not found"))
            Result.success(family)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getUserFamilies(): Flow<List<Family>> {
        return flow {
            authRepository.getCurrentUser().collect { currentUser ->
                if (currentUser != null) {
                    val families = familyDao.getFamiliesByUserId(currentUser.id).map { it.toDomain() }
                    emit(families)
                } else {
                    emit(emptyList())
                }
            }
        }
    }
    
    override suspend fun getPendingInvitations(email: String): List<Invitation> {
        return try {
            familyDao.getPendingInvitationsForEmail(email)
                .map { it.toDomain() }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun shareContent(contentId: String, contentType: String, targetUsers: List<String>): Result<Unit> {
        return try {
            val currentUser = authRepository.getCurrentUser().first()
                ?: return Result.failure(Exception("User not authenticated"))
                
            val sharedContent = SharedContent(
                id = UUID.randomUUID().toString(),
                contentId = contentId,
                contentType = SharedContentType.valueOf(contentType),
                ownerId = currentUser.id,
                sharedWith = targetUsers,
                familyId = "", // 家族IDは別途設定
                permissions = SharedPermissions(),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            
            firebaseFamilyDataSource.shareContent(sharedContent)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getSharedContent(): List<SharedContent> {
        return try {
            val currentUser = authRepository.getCurrentUser().first()
            if (currentUser != null) {
                firebaseFamilyDataSource.getSharedContent(currentUser.id)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun unshareContent(contentId: String): Result<Unit> {
        return try {
            firebaseFamilyDataSource.unshareContent(contentId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun getOrCreateFamily(familyName: String, userId: String): Family {
        val existingFamilies = familyDao.getFamiliesByUserId(userId)
        return if (existingFamilies.isNotEmpty()) {
            existingFamilies.first().toDomain()
        } else {
            createFamily(familyName).getOrThrow()
        }
    }
}

// Extension functions for mapping
private fun Family.toEntity(): FamilyEntity {
    return FamilyEntity(
        id = id,
        name = name,
        createdBy = createdBy,
        members = members,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

private fun FamilyEntity.toDomain(): Family {
    return Family(
        id = id,
        name = name,
        createdBy = createdBy,
        members = members,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

private fun Invitation.toEntity(): InvitationEntity {
    return InvitationEntity(
        id = id,
        familyId = familyId,
        inviterUserId = inviterUserId,
        inviterName = inviterName,
        inviteeEmail = inviteeEmail,
        status = status.name,
        createdAt = createdAt,
        expiresAt = expiresAt
    )
}

private fun InvitationEntity.toDomain(): Invitation {
    return Invitation(
        id = id,
        familyId = familyId,
        inviterUserId = inviterUserId,
        inviterName = inviterName,
        inviteeEmail = inviteeEmail,
        status = InvitationStatus.valueOf(status),
        createdAt = createdAt,
        expiresAt = expiresAt
    )
}