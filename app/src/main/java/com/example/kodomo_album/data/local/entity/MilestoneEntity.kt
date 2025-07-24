package com.example.kodomo_album.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "milestones",
    foreignKeys = [
        ForeignKey(
            entity = ChildEntity::class,
            parentColumns = ["id"],
            childColumns = ["childId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["childId"]), Index(value = ["achievedAt"])]
)
data class MilestoneEntity(
    @PrimaryKey
    val id: String,
    val childId: String,
    val type: String, // "MOTOR", "LANGUAGE", "SOCIAL", "COGNITIVE"
    val title: String,
    val description: String,
    val achievedAt: Long, // Unix timestamp (日付のみ)
    val mediaIds: String, // JSON配列として保存 ["id1", "id2"]
    val createdAt: Long,
    val isSynced: Boolean = false
)