package com.example.kodomo_album.presentation.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.kodomo_album.core.util.Resource
import com.example.kodomo_album.core.util.UiEvent
import com.example.kodomo_album.domain.usecase.auth.SignInUseCase
import com.example.kodomo_album.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase
) : BaseViewModel() {

    var state by mutableStateOf(LoginState())
        private set

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> {
                state = state.copy(email = event.email)
            }
            is LoginEvent.PasswordChanged -> {
                state = state.copy(password = event.password)
            }
            is LoginEvent.Login -> {
                login()
            }
            is LoginEvent.NavigateToSignUp -> {
                sendUiEvent(UiEvent.Navigate("signup"))
            }
            is LoginEvent.NavigateToPasswordReset -> {
                sendUiEvent(UiEvent.Navigate("password_reset"))
            }
        }
    }

    private fun login() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            
            when (val result = signInUseCase(state.email, state.password)) {
                is Resource.Success -> {
                    state = state.copy(isLoading = false)
                    sendUiEvent(UiEvent.Navigate("profile"))
                }
                is Resource.Error -> {
                    state = state.copy(isLoading = false)
                    sendUiEvent(UiEvent.ShowSnackbar(result.message ?: "ログインに失敗しました"))
                }
                is Resource.Loading -> {
                    state = state.copy(isLoading = true)
                }
            }
        }
    }
}

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false
)

sealed class LoginEvent {
    data class EmailChanged(val email: String) : LoginEvent()
    data class PasswordChanged(val password: String) : LoginEvent()
    object Login : LoginEvent()
    object NavigateToSignUp : LoginEvent()
    object NavigateToPasswordReset : LoginEvent()
}