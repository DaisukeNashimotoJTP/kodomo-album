package com.example.kodomoalbum.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.kodomo_album.data.local.DatabaseConverters
import com.example.kodomoalbum.data.local.converter.FamilyMemberConverter
import com.example.kodomoalbum.domain.model.FamilyMember
import java.time.LocalDateTime

@Entity(tableName = "families")
@TypeConverters(DatabaseConverters::class, FamilyMemberConverter::class)
data class FamilyEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val createdBy: String,
    val members: List<FamilyMember>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

@Entity(tableName = "invitations")
@TypeConverters(DatabaseConverters::class)
data class InvitationEntity(
    @PrimaryKey
    val id: String,
    val familyId: String,
    val inviterUserId: String,
    val inviterName: String,
    val inviteeEmail: String,
    val status: String, // InvitationStatus.name
    val createdAt: LocalDateTime,
    val expiresAt: LocalDateTime
)