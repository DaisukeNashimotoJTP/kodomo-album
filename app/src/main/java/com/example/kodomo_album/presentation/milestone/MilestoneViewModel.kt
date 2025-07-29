package com.example.kodomo_album.presentation.milestone

import androidx.lifecycle.viewModelScope
import com.example.kodomo_album.core.util.Resource
import com.example.kodomo_album.core.util.UiEvent
import com.example.kodomo_album.domain.model.Milestone
import com.example.kodomo_album.domain.model.MilestoneType
import com.example.kodomo_album.domain.usecase.milestone.CreateMilestoneUseCase
import com.example.kodomo_album.domain.usecase.milestone.GetMilestonesUseCase
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

data class MilestoneUiState(
    val milestones: List<Milestone> = emptyList(),
    val title: String = "",
    val description: String = "",
    val selectedType: MilestoneType = MilestoneType.MOTOR,
    val achievedAt: LocalDate = LocalDate.now(),
    val mediaIds: List<String> = emptyList(),
    val isFormValid: Boolean = false
)

@HiltViewModel
class MilestoneViewModel @Inject constructor(
    private val createMilestoneUseCase: CreateMilestoneUseCase,
    private val getMilestonesUseCase: GetMilestonesUseCase
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(MilestoneUiState())
    val uiState: StateFlow<MilestoneUiState> = _uiState.asStateFlow()

    private var currentChildId: String = ""

    fun loadMilestones(childId: String) {
        currentChildId = childId
        viewModelScope.launch {
            try {
                getMilestonesUseCase(childId).collect { milestones ->
                    _uiState.value = _uiState.value.copy(
                        milestones = milestones
                    )
                }
            } catch (e: Exception) {
                sendUiEvent(UiEvent.ShowSnackbar(e.message ?: "マイルストーンの読み込みに失敗しました"))
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

    fun onTypeChanged(type: MilestoneType) {
        _uiState.value = _uiState.value.copy(selectedType = type)
    }

    fun onAchievedAtChanged(date: LocalDate) {
        _uiState.value = _uiState.value.copy(achievedAt = date)
    }

    fun onMediaIdsChanged(mediaIds: List<String>) {
        _uiState.value = _uiState.value.copy(mediaIds = mediaIds)
    }

    fun createMilestone() {
        if (!_uiState.value.isFormValid) return

        _isLoading.value = true
        
        viewModelScope.launch {
            val milestone = Milestone(
                id = UUID.randomUUID().toString(),
                childId = currentChildId,
                type = _uiState.value.selectedType,
                title = _uiState.value.title,
                description = _uiState.value.description,
                achievedAt = _uiState.value.achievedAt,
                mediaIds = _uiState.value.mediaIds,
                createdAt = LocalDateTime.now()
            )

            when (val result = createMilestoneUseCase(milestone)) {
                is Resource.Success -> {
                    sendUiEvent(UiEvent.ShowSnackbar("マイルストーンを保存しました"))
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
            selectedType = MilestoneType.MOTOR,
            achievedAt = LocalDate.now(),
            mediaIds = emptyList(),
            isFormValid = false
        )
    }
}