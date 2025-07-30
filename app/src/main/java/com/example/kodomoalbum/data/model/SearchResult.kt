package com.example.kodomoalbum.data.model

import java.time.LocalDateTime

data class SearchResult(
    val id: String,
    val type: SearchResultType,
    val title: String,
    val content: String,
    val date: LocalDateTime,
    val thumbnailUrl: String? = null,
    val childId: String
)

enum class SearchResultType {
    DIARY,
    MEDIA,
    EVENT,
    MILESTONE
}

data class SearchFilter(
    val dateRange: DateRange? = null,
    val types: List<SearchResultType> = emptyList(),
    val categories: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val childIds: List<String> = emptyList()
)

data class SearchHistory(
    val id: String,
    val query: String,
    val searchedAt: LocalDateTime,
    val resultsCount: Int
)