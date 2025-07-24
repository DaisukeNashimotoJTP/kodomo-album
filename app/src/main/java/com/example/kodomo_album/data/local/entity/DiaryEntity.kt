package com.example.kodomo_album.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "diaries",
    foreignKeys = [
        ForeignKey(
            entity = ChildEntity::class,
            parentColumns = ["id"],
            childColumns = ["childId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["childId"]), Index(value = ["date"])]
)
data class DiaryEntity(
    @PrimaryKey
    val id: String,
    val childId: String,
    val title: String,
    val content: String,
    val mediaIds: String, // JSON配列として保存 ["id1", "id2"]
    val date: Long, // Unix timestamp (日付のみ)
    val createdAt: Long,
    val updatedAt: Long,
    val isSynced: Boolean = false // Firestoreとの同期状態
)