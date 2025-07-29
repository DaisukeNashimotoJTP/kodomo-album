package com.example.kodomo_album.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

enum class MilestoneType(val displayName: String) {
    MOTOR("運動"),
    LANGUAGE("言語"),
    SOCIAL("社会性"),
    COGNITIVE("認知")
}

data class Milestone(
    val id: String,
    val childId: String,
    val type: MilestoneType,
    val title: String,
    val description: String,
    val achievedAt: LocalDate,
    val mediaIds: List<String> = emptyList(),
    val createdAt: LocalDateTime
)