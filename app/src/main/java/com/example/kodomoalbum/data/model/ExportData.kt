package com.example.kodomoalbum.data.model

import java.time.LocalDate

data class ExportRequest(
    val childId: String,
    val exportType: ExportType,
    val dateRange: DateRange? = null,
    val includePhotos: Boolean = true,
    val includeDiaries: Boolean = true,
    val includeEvents: Boolean = true,
    val includeMilestones: Boolean = true,
    val includeGrowthRecords: Boolean = true
)

enum class ExportType {
    PDF,
    BACKUP_JSON
}

data class ExportResult(
    val success: Boolean,
    val filePath: String? = null,
    val error: String? = null,
    val fileSize: Long = 0
)

data class ExportProgress(
    val currentStep: String,
    val progress: Float, // 0.0 to 1.0
    val isCompleted: Boolean = false
)