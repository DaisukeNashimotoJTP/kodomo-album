package com.example.kodomoalbum.presentation.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kodomoalbum.data.model.ExportProgress
import com.example.kodomoalbum.data.model.ExportRequest
import com.example.kodomoalbum.data.model.ExportType
import com.example.kodomoalbum.domain.usecase.ExportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val exportUseCase: ExportUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ExportUiState())
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()
    
    private val _exportProgress = MutableStateFlow<ExportProgress?>(null)
    val exportProgress: StateFlow<ExportProgress?> = _exportProgress.asStateFlow()
    
    fun updateExportRequest(request: ExportRequest) {
        _uiState.value = _uiState.value.copy(exportRequest = request)
    }
    
    fun startExport() {
        val request = _uiState.value.exportRequest
        _uiState.value = _uiState.value.copy(isExporting = true, error = null)
        
        viewModelScope.launch {
            val exportFlow = when (request.exportType) {
                ExportType.PDF -> exportUseCase.exportToPdf(request)
                ExportType.BACKUP_JSON -> exportUseCase.exportToBackup(request)
            }
            
            exportFlow
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isExporting = false,
                        error = exception.message ?: "エクスポートに失敗しました"
                    )
                    _exportProgress.value = null
                }
                .collect { progress ->
                    _exportProgress.value = progress
                    if (progress.isCompleted) {
                        _uiState.value = _uiState.value.copy(
                            isExporting = false,
                            isCompleted = true
                        )
                    }
                }
        }
    }
    
    fun resetExport() {
        _uiState.value = _uiState.value.copy(
            isExporting = false,
            isCompleted = false,
            error = null
        )
        _exportProgress.value = null
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class ExportUiState(
    val exportRequest: ExportRequest = ExportRequest(
        childId = "",
        exportType = ExportType.PDF
    ),
    val isExporting: Boolean = false,
    val isCompleted: Boolean = false,
    val error: String? = null
)