package com.example.kodomo_album.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "children",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class ChildEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val name: String,
    val birthDate: Long, // Unix timestamp
    val gender: String, // "MALE", "FEMALE", "OTHER"
    val profileImageUrl: String?,
    val createdAt: Long,
    val updatedAt: Long
)