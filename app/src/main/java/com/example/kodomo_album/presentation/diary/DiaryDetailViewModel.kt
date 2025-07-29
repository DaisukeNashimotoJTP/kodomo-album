package com.example.kodomo_album.presentation.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kodomo_album.domain.model.Diary
import com.example.kodomo_album.domain.repository.DiaryRepository
import com.example.kodomo_album.core.util.Resource
import com.example.kodomo_album.core.util.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

data class DiaryDetailUiState(
    val diary: Diary? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class DiaryDetailViewModel @Inject constructor(
    private val diaryRepository: DiaryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiaryDetailUiState())
    val uiState: StateFlow<DiaryDetailUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun loadDiary(diaryId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            when (val result = diaryRepository.getDiaryById(diaryId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        diary = result.data,
                        isLoading = false
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                    _uiEvent.send(UiEvent.ShowSnackbar(result.message ?: "日記の読み込みに失敗しました"))
                }
                is Resource.Loading -> {
                    // Loading state already handled above
                }
            }
        }
    }
}