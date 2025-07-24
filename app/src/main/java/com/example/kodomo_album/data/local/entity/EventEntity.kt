package com.example.kodomo_album.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "events",
    foreignKeys = [
        ForeignKey(
            entity = ChildEntity::class,
            parentColumns = ["id"],
            childColumns = ["childId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["childId"]), Index(value = ["eventDate"])]
)
data class EventEntity(
    @PrimaryKey
    val id: String,
    val childId: String,
    val title: String,
    val description: String,
    val eventDate: Long, // Unix timestamp (日付のみ)
    val mediaIds: String, // JSON配列として保存 ["id1", "id2"]
    val eventType: String, // "BIRTHDAY", "FIRST_STEP", "CEREMONY", "CUSTOM"
    val createdAt: Long,
    val updatedAt: Long,
    val isSynced: Boolean = false
)