package com.example.kodomoalbum.domain.model

import java.time.LocalDateTime

/**
 * 共有されたコンテンツを表すデータクラス
 */
data class SharedContent(
    val id: String,
    val contentId: String,
    val contentType: SharedContentType,
    val ownerId: String,
    val sharedWith: List<String>, // ユーザーIDのリスト
    val familyId: String,
    val permissions: SharedPermissions,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

/**
 * 共有コンテンツのタイプ
 */
enum class SharedContentType {
    PHOTO,
    VIDEO,
    DIARY,
    GROWTH_RECORD,
    MILESTONE,
    EVENT
}

/**
 * 共有権限を表すデータクラス
 */
data class SharedPermissions(
    val canView: Boolean = true,
    val canEdit: Boolean = false,
    val canDelete: Boolean = false,
    val canComment: Boolean = true
)