package com.example.kodomo_album.presentation.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.kodomo_album.core.util.Resource
import com.example.kodomo_album.core.util.UiEvent
import com.example.kodomo_album.domain.usecase.auth.SignUpUseCase
import com.example.kodomo_album.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase
) : BaseViewModel() {

    var state by mutableStateOf(SignUpState())
        private set

    fun onEvent(event: SignUpEvent) {
        when (event) {
            is SignUpEvent.EmailChanged -> {
                state = state.copy(email = event.email)
            }
            is SignUpEvent.PasswordChanged -> {
                state = state.copy(password = event.password)
            }
            is SignUpEvent.ConfirmPasswordChanged -> {
                state = state.copy(confirmPassword = event.confirmPassword)
            }
            is SignUpEvent.DisplayNameChanged -> {
                state = state.copy(displayName = event.displayName)
            }
            is SignUpEvent.SignUp -> {
                signUp()
            }
            is SignUpEvent.NavigateToLogin -> {
                sendUiEvent(UiEvent.NavigateUp)
            }
        }
    }

    private fun signUp() {
        if (state.password != state.confirmPassword) {
            sendUiEvent(UiEvent.ShowSnackbar("パスワードが一致しません"))
            return
        }

        viewModelScope.launch {
            state = state.copy(isLoading = true)
            
            when (val result = signUpUseCase(state.email, state.password, state.displayName)) {
                is Resource.Success -> {
                    state = state.copy(isLoading = false)
                    sendUiEvent(UiEvent.Navigate("profile"))
                }
                is Resource.Error -> {
                    state = state.copy(isLoading = false)
                    sendUiEvent(UiEvent.ShowSnackbar(result.message ?: "サインアップに失敗しました"))
                }
                is Resource.Loading -> {
                    state = state.copy(isLoading = true)
                }
            }
        }
    }
}

data class SignUpState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val displayName: String = "",
    val isLoading: Boolean = false
)

sealed class SignUpEvent {
    data class EmailChanged(val email: String) : SignUpEvent()
    data class PasswordChanged(val password: String) : SignUpEvent()
    data class ConfirmPasswordChanged(val confirmPassword: String) : SignUpEvent()
    data class DisplayNameChanged(val displayName: String) : SignUpEvent()
    object SignUp : SignUpEvent()
    object NavigateToLogin : SignUpEvent()
}