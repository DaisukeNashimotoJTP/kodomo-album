package com.example.kodomo_album.domain.model

import java.time.LocalDate

data class GrowthRecord(
    val id: String,
    val childId: String,
    val height: Double?, // cm
    val weight: Double?, // kg
    val headCircumference: Double?, // cm
    val recordedAt: LocalDate,
    val notes: String? = null
)

data class GrowthChartData(
    val heightData: List<GrowthDataPoint>,
    val weightData: List<GrowthDataPoint>,
    val headCircumferenceData: List<GrowthDataPoint>,
    val standardHeightData: List<GrowthDataPoint> = emptyList(),
    val standardWeightData: List<GrowthDataPoint> = emptyList()
)

data class GrowthDataPoint(
    val date: LocalDate,
    val value: Double,
    val ageInMonths: Int = 0
)

data class GrowthSummary(
    val childId: String,
    val period: String,
    val latestHeight: Double?,
    val latestWeight: Double?,
    val latestHeadCircumference: Double?,
    val heightGrowth: Double?,
    val weightGrowth: Double?,
    val totalRecords: Int,
    val recordPeriod: LocalDate
)

enum class GrowthType {
    HEIGHT,
    WEIGHT,
    HEAD_CIRCUMFERENCE,
    ALL
}

enum class GrowthPeriod {
    WEEK,
    MONTH,
    THREE_MONTHS,
    SIX_MONTHS,
    YEAR,
    ALL
}