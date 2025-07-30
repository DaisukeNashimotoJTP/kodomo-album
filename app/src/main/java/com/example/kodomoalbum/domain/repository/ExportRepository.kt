package com.example.kodomoalbum.domain.repository

import com.example.kodomoalbum.data.model.ExportProgress
import com.example.kodomoalbum.data.model.ExportRequest
import com.example.kodomoalbum.data.model.ExportResult
import kotlinx.coroutines.flow.Flow

interface ExportRepository {
    
    suspend fun exportToPdf(request: ExportRequest): Flow<ExportProgress>
    
    suspend fun exportToBackup(request: ExportRequest): Flow<ExportProgress>
    
    suspend fun getExportResult(requestId: String): ExportResult?
}