package com.example.kodomo_album.core.util

sealed class UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent()
    data class Navigate(val route: String) : UiEvent()
    object NavigateUp : UiEvent()
}