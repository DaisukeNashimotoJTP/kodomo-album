package com.example.kodomoalbum.presentation.ui.sharing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kodomoalbum.domain.model.Family
import com.example.kodomoalbum.domain.repository.SharingRepository
import com.example.kodomoalbum.domain.usecase.sharing.GetPendingInvitationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FamilyManagementViewModel @Inject constructor(
    private val sharingRepository: SharingRepository,
    private val getPendingInvitationsUseCase: GetPendingInvitationsUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(FamilyManagementUiState())
    val uiState: StateFlow<FamilyManagementUiState> = _uiState.asStateFlow()
    
    init {
        observeFamilies()
        loadPendingInvitationsCount()
    }
    
    private fun observeFamilies() {
        viewModelScope.launch {
            sharingRepository.getUserFamilies().collect { families ->
                _uiState.value = _uiState.value.copy(
                    families = families,
                    isLoading = false
                )
            }
        }
    }
    
    fun loadFamilies() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        // Families are observed automatically
    }
    
    private fun loadPendingInvitationsCount() {
        viewModelScope.launch {
            try {
                val invitations = getPendingInvitationsUseCase()
                _uiState.value = _uiState.value.copy(
                    pendingInvitationsCount = invitations.size
                )
            } catch (e: Exception) {
                // Silently handle error for invitation count
                _uiState.value = _uiState.value.copy(
                    pendingInvitationsCount = 0
                )
            }
        }
    }
    
    fun leaveFamily(familyId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                leavingFamilyId = familyId,
                error = null
            )
            
            // TODO: Implement leave family functionality
            // For now, just simulate the action
            kotlinx.coroutines.delay(1000)
            
            _uiState.value = _uiState.value.copy(
                leavingFamilyId = null
            )
        }
    }
}

data class FamilyManagementUiState(
    val isLoading: Boolean = false,
    val families: List<Family> = emptyList(),
    val pendingInvitationsCount: Int = 0,
    val leavingFamilyId: String? = null,
    val error: String? = null
)