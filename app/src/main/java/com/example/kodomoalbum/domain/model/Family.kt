package com.example.kodomoalbum.domain.model

import java.time.LocalDateTime

/**
 * 家族関係を表すデータクラス
 */
data class Family(
    val id: String,
    val name: String,
    val createdBy: String,
    val members: List<FamilyMember>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

/**
 * 家族メンバーを表すデータクラス
 */
data class FamilyMember(
    val userId: String,
    val email: String,
    val name: String,
    val role: FamilyRole,
    val joinedAt: LocalDateTime
)

/**
 * 家族での役割
 */
enum class FamilyRole {
    ADMIN,      // 家族作成者
    MEMBER      // 招待されたメンバー
}

/**
 * 招待状を表すデータクラス
 */
data class Invitation(
    val id: String,
    val familyId: String,
    val inviterUserId: String,
    val inviterName: String,
    val inviteeEmail: String,
    val status: InvitationStatus,
    val createdAt: LocalDateTime,
    val expiresAt: LocalDateTime
)

/**
 * 招待状のステータス
 */
enum class InvitationStatus {
    PENDING,    // 招待中
    ACCEPTED,   // 受諾済み
    DECLINED,   // 拒否済み
    EXPIRED     // 期限切れ
}