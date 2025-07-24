package com.example.kodomo_album.presentation.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.kodomo_album.core.util.Resource
import com.example.kodomo_album.core.util.UiEvent
import com.example.kodomo_album.data.repository.AuthRepository
import com.example.kodomo_album.domain.model.User
import com.example.kodomo_album.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : BaseViewModel() {

    var state by mutableStateOf(ProfileState())
        private set

    init {
        getCurrentUser()
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.DisplayNameChanged -> {
                state = state.copy(displayName = event.displayName)
            }
            is ProfileEvent.UpdateProfile -> {
                updateProfile()
            }
            is ProfileEvent.SignOut -> {
                signOut()
            }
            is ProfileEvent.NavigateToEditProfile -> {
                state = state.copy(isEditing = true)
            }
            is ProfileEvent.CancelEdit -> {
                state = state.copy(
                    isEditing = false,
                    displayName = state.user?.displayName ?: ""
                )
            }
        }
    }

    private fun getCurrentUser() {
        viewModelScope.launch {
            authRepository.getCurrentUser().collect { user ->
                state = state.copy(
                    user = user,
                    displayName = user?.displayName ?: "",
                    isLoading = false
                )
            }
        }
    }

    private fun updateProfile() {
        val currentUser = state.user ?: return
        
        viewModelScope.launch {
            state = state.copy(isUpdating = true)
            
            val updatedUser = currentUser.copy(
                displayName = state.displayName
            )
            
            when (val result = authRepository.updateUserProfile(updatedUser)) {
                is Resource.Success -> {
                    state = state.copy(
                        isUpdating = false,
                        isEditing = false,
                        user = result.data
                    )
                    sendUiEvent(UiEvent.ShowSnackbar("プロフィールを更新しました"))
                }
                is Resource.Error -> {
                    state = state.copy(isUpdating = false)
                    sendUiEvent(UiEvent.ShowSnackbar(result.message ?: "プロフィール更新に失敗しました"))
                }
                is Resource.Loading -> {
                    state = state.copy(isUpdating = true)
                }
            }
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            when (val result = authRepository.signOut()) {
                is Resource.Success -> {
                    sendUiEvent(UiEvent.Navigate("login"))
                }
                is Resource.Error -> {
                    sendUiEvent(UiEvent.ShowSnackbar(result.message ?: "ログアウトに失敗しました"))
                }
                is Resource.Loading -> Unit
            }
        }
    }
}

data class ProfileState(
    val user: User? = null,
    val displayName: String = "",
    val isLoading: Boolean = true,
    val isUpdating: Boolean = false,
    val isEditing: Boolean = false
)

sealed class ProfileEvent {
    data class DisplayNameChanged(val displayName: String) : ProfileEvent()
    object UpdateProfile : ProfileEvent()
    object SignOut : ProfileEvent()
    object NavigateToEditProfile : ProfileEvent()
    object CancelEdit : ProfileEvent()
}