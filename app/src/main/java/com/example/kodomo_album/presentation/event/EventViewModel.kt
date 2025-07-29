package com.example.kodomo_album.presentation.event

import androidx.lifecycle.viewModelScope
import com.example.kodomo_album.core.util.Resource
import com.example.kodomo_album.core.util.UiEvent
import com.example.kodomo_album.domain.model.Event
import com.example.kodomo_album.domain.model.EventType
import com.example.kodomo_album.domain.usecase.event.CreateEventUseCase
import com.example.kodomo_album.domain.usecase.event.GetEventsUseCase
import com.example.kodomo_album.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

data class EventUiState(
    val events: List<Event> = emptyList(),
    val title: String = "",
    val description: String = "",
    val selectedType: EventType = EventType.CUSTOM,
    val eventDate: LocalDate = LocalDate.now(),
    val mediaIds: List<String> = emptyList(),
    val isFormValid: Boolean = false
)

@HiltViewModel
class EventViewModel @Inject constructor(
    private val createEventUseCase: CreateEventUseCase,
    private val getEventsUseCase: GetEventsUseCase
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(EventUiState())
    val uiState: StateFlow<EventUiState> = _uiState.asStateFlow()

    private var currentChildId: String = ""

    fun loadEvents(childId: String) {
        currentChildId = childId
        viewModelScope.launch {
            try {
                getEventsUseCase(childId).collect { events ->
                    _uiState.value = _uiState.value.copy(
                        events = events
                    )
                }
            } catch (e: Exception) {
                sendUiEvent(UiEvent.ShowSnackbar(e.message ?: "イベントの読み込みに失敗しました"))
            }
        }
    }

    fun onTitleChanged(title: String) {
        _uiState.value = _uiState.value.copy(
            title = title,
            isFormValid = validateForm(title, _uiState.value.description)
        )
    }

    fun onDescriptionChanged(description: String) {
        _uiState.value = _uiState.value.copy(
            description = description,
            isFormValid = validateForm(_uiState.value.title, description)
        )
    }

    fun onTypeChanged(type: EventType) {
        _uiState.value = _uiState.value.copy(selectedType = type)
    }

    fun onEventDateChanged(date: LocalDate) {
        _uiState.value = _uiState.value.copy(eventDate = date)
    }

    fun onMediaIdsChanged(mediaIds: List<String>) {
        _uiState.value = _uiState.value.copy(mediaIds = mediaIds)
    }

    fun createEvent() {
        if (!_uiState.value.isFormValid) return

        _isLoading.value = true
        
        viewModelScope.launch {
            val now = LocalDateTime.now()
            val event = Event(
                id = UUID.randomUUID().toString(),
                childId = currentChildId,
                title = _uiState.value.title,
                description = _uiState.value.description,
                eventDate = _uiState.value.eventDate,
                mediaIds = _uiState.value.mediaIds,
                eventType = _uiState.value.selectedType,
                createdAt = now,
                updatedAt = now
            )

            when (val result = createEventUseCase(event)) {
                is Resource.Success -> {
                    sendUiEvent(UiEvent.ShowSnackbar("イベントを保存しました"))
                    clearForm()
                    sendUiEvent(UiEvent.NavigateUp)
                }
                is Resource.Error -> {
                    sendUiEvent(UiEvent.ShowSnackbar(result.message ?: "保存に失敗しました"))
                }
                is Resource.Loading -> {
                    // 処理中
                }
            }
            
            _isLoading.value = false
        }
    }

    private fun validateForm(title: String, description: String): Boolean {
        return title.isNotBlank() && description.isNotBlank()
    }

    private fun clearForm() {
        _uiState.value = _uiState.value.copy(
            title = "",
            description = "",
            selectedType = EventType.CUSTOM,
            eventDate = LocalDate.now(),
            mediaIds = emptyList(),
            isFormValid = false
        )
    }
}