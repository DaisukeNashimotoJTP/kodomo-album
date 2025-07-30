package com.example.kodomoalbum.data.model

import com.example.kodomo_album.domain.model.Child
import java.time.LocalDate
import java.time.LocalDateTime

data class DashboardData(
    val child: Child,
    val recentActivities: List<RecentActivity>,
    val growthSummary: GrowthSummary,
    val statistics: Statistics,
    val upcomingEvents: List<Event>
)

data class RecentActivity(
    val id: String,
    val type: ActivityType,
    val title: String,
    val description: String,
    val date: LocalDateTime,
    val thumbnailUrl: String? = null
)

enum class ActivityType {
    PHOTO_ADDED,
    DIARY_CREATED,
    MILESTONE_ACHIEVED,
    EVENT_RECORDED,
    GROWTH_RECORDED
}

data class GrowthSummary(
    val currentAge: Int, // 月齢
    val latestHeight: Double?,
    val latestWeight: Double?,
    val heightGrowth: Double?, // 前月比
    val weightGrowth: Double?, // 前月比
    val lastMeasuredDate: LocalDate?
)

data class Statistics(
    val totalPhotos: Int,
    val totalDiaries: Int,
    val totalEvents: Int,
    val totalMilestones: Int,
    val thisMonthPhotos: Int,
    val thisMonthDiaries: Int
)