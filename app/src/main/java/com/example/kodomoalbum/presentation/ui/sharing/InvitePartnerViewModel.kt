package com.example.kodomoalbum.presentation.ui.sharing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kodomoalbum.domain.model.Invitation
import com.example.kodomoalbum.domain.usecase.sharing.InvitePartnerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InvitePartnerViewModel @Inject constructor(
    private val invitePartnerUseCase: InvitePartnerUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(InvitePartnerUiState())
    val uiState: StateFlow<InvitePartnerUiState> = _uiState.asStateFlow()
    
    fun invitePartner(email: String, familyName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            invitePartnerUseCase(email, familyName)
                .onSuccess { invitation ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        invitation = invitation,
                        isSuccess = true
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Unknown error occurred"
                    )
                }
        }
    }
    
    fun clearState() {
        _uiState.value = InvitePartnerUiState()
    }
}

data class InvitePartnerUiState(
    val isLoading: Boolean = false,
    val invitation: Invitation? = null,
    val isSuccess: Boolean = false,
    val error: String? = null
)