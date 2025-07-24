package com.example.kodomo_album.presentation.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.kodomo_album.core.util.Resource
import com.example.kodomo_album.core.util.UiEvent
import com.example.kodomo_album.domain.usecase.auth.SendPasswordResetUseCase
import com.example.kodomo_album.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PasswordResetViewModel @Inject constructor(
    private val sendPasswordResetUseCase: SendPasswordResetUseCase
) : BaseViewModel() {

    var state by mutableStateOf(PasswordResetState())
        private set

    fun onEvent(event: PasswordResetEvent) {
        when (event) {
            is PasswordResetEvent.EmailChanged -> {
                state = state.copy(email = event.email)
            }
            is PasswordResetEvent.SendPasswordReset -> {
                sendPasswordReset()
            }
            is PasswordResetEvent.NavigateToLogin -> {
                sendUiEvent(UiEvent.NavigateUp)
            }
        }
    }

    private fun sendPasswordReset() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            
            when (val result = sendPasswordResetUseCase(state.email)) {
                is Resource.Success -> {
                    state = state.copy(isLoading = false, isEmailSent = true)
                    sendUiEvent(UiEvent.ShowSnackbar("パスワードリセットメールを送信しました"))
                }
                is Resource.Error -> {
                    state = state.copy(isLoading = false)
                    sendUiEvent(UiEvent.ShowSnackbar(result.message ?: "メール送信に失敗しました"))
                }
                is Resource.Loading -> {
                    state = state.copy(isLoading = true)
                }
            }
        }
    }
}

data class PasswordResetState(
    val email: String = "",
    val isLoading: Boolean = false,
    val isEmailSent: Boolean = false
)

sealed class PasswordResetEvent {
    data class EmailChanged(val email: String) : PasswordResetEvent()
    object SendPasswordReset : PasswordResetEvent()
    object NavigateToLogin : PasswordResetEvent()
}