package com.example.kodomo_album.domain.model

import java.time.LocalDateTime

data class Media(
    val id: String,
    val childId: String,
    val type: MediaType,
    val url: String,
    val thumbnailUrl: String?,
    val caption: String?,
    val takenAt: LocalDateTime,
    val uploadedAt: LocalDateTime
)

enum class MediaType {
    PHOTO,
    VIDEO,
    ECHO
}

data class MediaMetadata(
    val childId: String,
    val caption: String? = null,
    val type: MediaType = MediaType.PHOTO,
    val takenAt: LocalDateTime = LocalDateTime.now()
)