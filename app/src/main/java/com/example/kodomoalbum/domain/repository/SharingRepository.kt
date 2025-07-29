package com.example.kodomoalbum.domain.repository

import com.example.kodomoalbum.domain.model.Family
import com.example.kodomoalbum.domain.model.Invitation
import com.example.kodomoalbum.domain.model.SharedContent
import kotlinx.coroutines.flow.Flow

interface SharingRepository {
    
    /**
     * パートナーを招待する
     */
    suspend fun invitePartner(email: String, familyName: String): Result<Invitation>
    
    /**
     * 招待を受諾する
     */
    suspend fun acceptInvitation(invitationId: String): Result<Unit>
    
    /**
     * 招待を拒否する
     */
    suspend fun declineInvitation(invitationId: String): Result<Unit>
    
    /**
     * 家族を作成する
     */
    suspend fun createFamily(name: String): Result<Family>
    
    /**
     * 家族情報を取得する
     */
    suspend fun getFamily(familyId: String): Result<Family>
    
    /**
     * 現在のユーザーが所属する家族一覧を取得する
     */
    fun getUserFamilies(): Flow<List<Family>>
    
    /**
     * 招待一覧を取得する（メールアドレス基準）
     */
    suspend fun getPendingInvitations(email: String): List<Invitation>
    
    /**
     * コンテンツを共有する
     */
    suspend fun shareContent(contentId: String, contentType: String, targetUsers: List<String>): Result<Unit>
    
    /**
     * 共有されたコンテンツを取得する
     */
    suspend fun getSharedContent(): List<SharedContent>
    
    /**
     * 共有を解除する
     */
    suspend fun unshareContent(contentId: String): Result<Unit>
}