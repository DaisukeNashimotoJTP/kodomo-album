package com.example.kodomoalbum.domain.usecase

import com.example.kodomoalbum.data.model.ExportProgress
import com.example.kodomoalbum.data.model.ExportRequest
import com.example.kodomoalbum.data.model.ExportResult
import com.example.kodomoalbum.domain.repository.ExportRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportUseCase @Inject constructor(
    private val exportRepository: ExportRepository
) {
    
    suspend fun exportToPdf(request: ExportRequest): Flow<ExportProgress> {
        return exportRepository.exportToPdf(request)
    }
    
    suspend fun exportToBackup(request: ExportRequest): Flow<ExportProgress> {
        return exportRepository.exportToBackup(request)
    }
    
    suspend fun getExportResult(requestId: String): ExportResult? {
        return exportRepository.getExportResult(requestId)
    }
}