package com.example.kodomoalbum.presentation.ui.sharing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kodomoalbum.domain.model.SharedContent
import com.example.kodomoalbum.domain.usecase.sharing.GetSharedContentUseCase
import com.example.kodomoalbum.domain.repository.SharingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharedContentViewModel @Inject constructor(
    private val getSharedContentUseCase: GetSharedContentUseCase,
    private val sharingRepository: SharingRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SharedContentUiState())
    val uiState: StateFlow<SharedContentUiState> = _uiState.asStateFlow()
    
    fun loadSharedContent() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val sharedContent = getSharedContentUseCase()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    sharedContent = sharedContent
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }
    
    fun unshareContent(contentId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                unsharingContentId = contentId,
                error = null
            )
            
            sharingRepository.unshareContent(contentId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        unsharingContentId = null,
                        sharedContent = _uiState.value.sharedContent.filter { 
                            it.contentId != contentId 
                        }
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        unsharingContentId = null,
                        error = error.message ?: "Failed to unshare content"
                    )
                }
        }
    }
}

data class SharedContentUiState(
    val isLoading: Boolean = false,
    val sharedContent: List<SharedContent> = emptyList(),
    val unsharingContentId: String? = null,
    val error: String? = null
)