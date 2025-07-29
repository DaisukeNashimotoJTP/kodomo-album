package com.example.kodomoalbum.presentation.ui.sharing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kodomoalbum.domain.model.Invitation
import com.example.kodomoalbum.domain.usecase.sharing.AcceptInvitationUseCase
import com.example.kodomoalbum.domain.usecase.sharing.GetPendingInvitationsUseCase
import com.example.kodomoalbum.domain.repository.SharingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InvitationListViewModel @Inject constructor(
    private val getPendingInvitationsUseCase: GetPendingInvitationsUseCase,
    private val acceptInvitationUseCase: AcceptInvitationUseCase,
    private val sharingRepository: SharingRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(InvitationListUiState())
    val uiState: StateFlow<InvitationListUiState> = _uiState.asStateFlow()
    
    fun loadInvitations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val invitations = getPendingInvitationsUseCase()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    invitations = invitations
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }
    
    fun acceptInvitation(invitationId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                processingInvitationId = invitationId,
                error = null
            )
            
            acceptInvitationUseCase(invitationId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        processingInvitationId = null,
                        invitations = _uiState.value.invitations.filter { it.id != invitationId }
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        processingInvitationId = null,
                        error = error.message ?: "Failed to accept invitation"
                    )
                }
        }
    }
    
    fun declineInvitation(invitationId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                processingInvitationId = invitationId,
                error = null
            )
            
            sharingRepository.declineInvitation(invitationId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        processingInvitationId = null,
                        invitations = _uiState.value.invitations.filter { it.id != invitationId }
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        processingInvitationId = null,
                        error = error.message ?: "Failed to decline invitation"
                    )
                }
        }
    }
}

data class InvitationListUiState(
    val isLoading: Boolean = false,
    val invitations: List<Invitation> = emptyList(),
    val processingInvitationId: String? = null,
    val error: String? = null
)