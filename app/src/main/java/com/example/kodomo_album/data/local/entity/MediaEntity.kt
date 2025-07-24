package com.example.kodomo_album.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "media",
    foreignKeys = [
        ForeignKey(
            entity = ChildEntity::class,
            parentColumns = ["id"],
            childColumns = ["childId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["childId"]), Index(value = ["takenAt"])]
)
data class MediaEntity(
    @PrimaryKey
    val id: String,
    val childId: String,
    val type: String, // "PHOTO", "VIDEO", "ECHO"
    val url: String,
    val thumbnailUrl: String?,
    val caption: String?,
    val takenAt: Long, // Unix timestamp
    val uploadedAt: Long,
    val isUploaded: Boolean = false, // ローカル保存状態管理
    val localPath: String? = null // ローカルファイルパス
)