package com.example.kodomo_album.domain.model

import java.time.LocalDate

data class Child(
    val id: String,
    val userId: String,
    val name: String,
    val birthDate: LocalDate,
    val gender: Gender,
    val profileImageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class Gender {
    MALE, FEMALE, OTHER
}