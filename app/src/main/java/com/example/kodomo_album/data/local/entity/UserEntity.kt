package com.example.kodomo_album.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val email: String,
    val displayName: String,
    val profileImageUrl: String,
    val createdAt: Long,
    val updatedAt: Long,
    val familyId: String?,
    val isPartner: Boolean
)