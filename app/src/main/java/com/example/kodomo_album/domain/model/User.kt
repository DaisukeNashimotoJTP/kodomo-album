package com.example.kodomo_album.domain.model

data class User(
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val profileImageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val familyId: String? = null,
    val isPartner: Boolean = false
)