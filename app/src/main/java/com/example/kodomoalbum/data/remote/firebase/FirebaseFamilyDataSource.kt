package com.example.kodomoalbum.data.remote.firebase

import com.example.kodomoalbum.domain.model.Family
import com.example.kodomoalbum.domain.model.Invitation
import com.example.kodomoalbum.domain.model.SharedContent
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseFamilyDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val functions: FirebaseFunctions
) {
    
    private val familiesCollection = firestore.collection("families")
    private val invitationsCollection = firestore.collection("invitations")
    private val sharedContentCollection = firestore.collection("shared_content")
    
    suspend fun createFamily(family: Family) {
        val familyMap = mapOf(
            "id" to family.id,
            "name" to family.name,
            "createdBy" to family.createdBy,
            "members" to family.members.map { member ->
                mapOf(
                    "userId" to member.userId,
                    "email" to member.email,
                    "name" to member.name,
                    "role" to member.role.name,
                    "joinedAt" to member.joinedAt.toString()
                )
            },
            "createdAt" to family.createdAt.toString(),
            "updatedAt" to family.updatedAt.toString()
        )
        
        familiesCollection.document(family.id).set(familyMap).await()
    }
    
    suspend fun updateFamily(family: Family) {
        val familyMap = mapOf(
            "name" to family.name,
            "members" to family.members.map { member ->
                mapOf(
                    "userId" to member.userId,
                    "email" to member.email,
                    "name" to member.name,
                    "role" to member.role.name,
                    "joinedAt" to member.joinedAt.toString()
                )
            },
            "updatedAt" to family.updatedAt.toString()
        )
        
        familiesCollection.document(family.id).update(familyMap).await()
    }
    
    suspend fun getFamily(familyId: String): Family? {
        val document = familiesCollection.document(familyId).get().await()
        return if (document.exists()) {
            document.toObject(Family::class.java)
        } else {
            null
        }
    }
    
    suspend fun createInvitation(invitation: Invitation) {
        val invitationMap = mapOf(
            "id" to invitation.id,
            "familyId" to invitation.familyId,
            "inviterUserId" to invitation.inviterUserId,
            "inviterName" to invitation.inviterName,
            "inviteeEmail" to invitation.inviteeEmail,
            "status" to invitation.status.name,
            "createdAt" to invitation.createdAt.toString(),
            "expiresAt" to invitation.expiresAt.toString()
        )
        
        invitationsCollection.document(invitation.id).set(invitationMap).await()
    }
    
    suspend fun updateInvitation(invitation: Invitation) {
        val invitationMap = mapOf(
            "status" to invitation.status.name
        )
        
        invitationsCollection.document(invitation.id).update(invitationMap).await()
    }
    
    suspend fun sendInvitationEmail(invitation: Invitation) {
        val data = mapOf(
            "invitationId" to invitation.id,
            "inviterName" to invitation.inviterName,
            "inviteeEmail" to invitation.inviteeEmail,
            "familyName" to "家族", // TODO: 実際の家族名を取得
        )
        
        // Firebase Cloud Functionsを使用して招待メールを送信
        functions.getHttpsCallable("sendInvitationEmail")
            .call(data)
            .await()
    }
    
    suspend fun shareContent(sharedContent: SharedContent) {
        val contentMap = mapOf(
            "id" to sharedContent.id,
            "contentId" to sharedContent.contentId,
            "contentType" to sharedContent.contentType.name,
            "ownerId" to sharedContent.ownerId,
            "sharedWith" to sharedContent.sharedWith,
            "familyId" to sharedContent.familyId,
            "permissions" to mapOf(
                "canView" to sharedContent.permissions.canView,
                "canEdit" to sharedContent.permissions.canEdit,
                "canDelete" to sharedContent.permissions.canDelete,
                "canComment" to sharedContent.permissions.canComment
            ),
            "createdAt" to sharedContent.createdAt.toString(),
            "updatedAt" to sharedContent.updatedAt.toString()
        )
        
        sharedContentCollection.document(sharedContent.id).set(contentMap).await()
    }
    
    suspend fun getSharedContent(userId: String): List<SharedContent> {
        val query = sharedContentCollection
            .whereArrayContains("sharedWith", userId)
            .get()
            .await()
            
        return query.documents.mapNotNull { document ->
            document.toObject(SharedContent::class.java)
        }
    }
    
    suspend fun unshareContent(contentId: String) {
        val query = sharedContentCollection
            .whereEqualTo("contentId", contentId)
            .get()
            .await()
            
        query.documents.forEach { document ->
            document.reference.delete().await()
        }
    }
}