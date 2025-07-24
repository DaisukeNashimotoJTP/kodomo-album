package com.example.kodomo_album.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "growth_records",
    foreignKeys = [
        ForeignKey(
            entity = ChildEntity::class,
            parentColumns = ["id"],
            childColumns = ["childId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["childId"]), Index(value = ["recordedAt"])]
)
data class GrowthRecordEntity(
    @PrimaryKey
    val id: String,
    val childId: String,
    val height: Double?, // cm
    val weight: Double?, // kg
    val headCircumference: Double?, // cm
    val recordedAt: Long, // Unix timestamp (日付のみ)
    val notes: String?,
    val createdAt: Long,
    val isSynced: Boolean = false
)