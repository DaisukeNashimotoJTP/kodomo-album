package com.example.kodomo_album.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

data class Diary(
    val id: String,
    val childId: String,
    val title: String,
    val content: String,
    val mediaIds: List<String>,
    val date: LocalDate,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)