package com.example.kodomo_album.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

enum class EventType(val displayName: String) {
    BIRTHDAY("誕生日"),
    FIRST_STEP("初歩き"),
    CEREMONY("お食い初め・記念日"),
    CUSTOM("その他")
}

data class Event(
    val id: String,
    val childId: String,
    val title: String,
    val description: String,
    val eventDate: LocalDate,
    val mediaIds: List<String> = emptyList(),
    val eventType: EventType,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)