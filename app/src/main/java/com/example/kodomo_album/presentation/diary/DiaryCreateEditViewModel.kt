package com.example.kodomo_album.presentation.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kodomo_album.domain.model.Diary
import com.example.kodomo_album.domain.usecase.diary.CreateDiaryUseCase
import com.example.kodomo_album.domain.usecase.diary.UpdateDiaryUseCase
import com.example.kodomo_album.domain.usecase.diary.DeleteDiaryUseCase
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
import java.time.LocalDate
import javax.inject.Inject

data class DiaryCreateEditUiState(
    val isLoading: Boolean = false,
    val title: String = "",
    val content: String = "",
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedMediaIds: List<String> = emptyList(),
    val isEditMode: Boolean = false,
    val currentDiary: Diary? = null
)

@HiltViewModel
class DiaryCreateEditViewModel @Inject constructor(
    private val createDiaryUseCase: CreateDiaryUseCase,
    private val updateDiaryUseCase: UpdateDiaryUseCase,
    private val deleteDiaryUseCase: DeleteDiaryUseCase,
    private val diaryRepository: DiaryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiaryCreateEditUiState())
    val uiState: StateFlow<DiaryCreateEditUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun loadDiary(diaryId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            when (val result = diaryRepository.getDiaryById(diaryId)) {
                is Resource.Success -> {
                    val diary = result.data
                    if (diary != null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            title = diary.title,
                            content = diary.content,
                            selectedDate = diary.date,
                            selectedMediaIds = diary.mediaIds,
                            isEditMode = true,
                            currentDiary = diary
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        _uiEvent.send(UiEvent.ShowSnackbar("日記が見つかりません"))
                    }
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _uiEvent.send(UiEvent.ShowSnackbar(result.message ?: "日記の読み込みに失敗しました"))
                }
                is Resource.Loading -> {
                    // Loading state already handled above
                }
            }
        }
    }

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun updateContent(content: String) {
        _uiState.value = _uiState.value.copy(content = content)
    }

    fun updateSelectedDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
    }

    fun addMedia(mediaId: String) {
        val currentMediaIds = _uiState.value.selectedMediaIds.toMutableList()
        if (!currentMediaIds.contains(mediaId)) {
            currentMediaIds.add(mediaId)
            _uiState.value = _uiState.value.copy(selectedMediaIds = currentMediaIds)
        }
    }

    fun removeMedia(mediaId: String) {
        val currentMediaIds = _uiState.value.selectedMediaIds.toMutableList()
        currentMediaIds.remove(mediaId)
        _uiState.value = _uiState.value.copy(selectedMediaIds = currentMediaIds)
    }

    fun saveDiary(childId: String) {
        val state = _uiState.value
        
        if (state.title.isBlank()) {
            viewModelScope.launch {
                _uiEvent.send(UiEvent.ShowSnackbar("タイトルを入力してください"))
            }
            return
        }

        if (state.content.isBlank()) {
            viewModelScope.launch {
                _uiEvent.send(UiEvent.ShowSnackbar("内容を入力してください"))
            }
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val result = if (state.isEditMode && state.currentDiary != null) {
                updateDiaryUseCase(
                    diary = state.currentDiary!!,
                    newTitle = state.title,
                    newContent = state.content,
                    newMediaIds = state.selectedMediaIds
                )
            } else {
                createDiaryUseCase(
                    childId = childId,
                    title = state.title,
                    content = state.content,
                    mediaIds = state.selectedMediaIds,
                    date = state.selectedDate
                )
            }

            when (result) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _uiEvent.send(UiEvent.ShowSnackbar(
                        if (state.isEditMode) "日記を更新しました" else "日記を保存しました"
                    ))
                    _uiEvent.send(UiEvent.NavigateUp)
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _uiEvent.send(UiEvent.ShowSnackbar(result.message ?: "保存に失敗しました"))
                }
                is Resource.Loading -> {
                    // Loading state already handled above
                }
            }
        }
    }

    fun deleteDiary() {
        val currentDiary = _uiState.value.currentDiary ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            when (val result = deleteDiaryUseCase(currentDiary.id)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _uiEvent.send(UiEvent.ShowSnackbar("日記を削除しました"))
                    _uiEvent.send(UiEvent.NavigateUp)
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _uiEvent.send(UiEvent.ShowSnackbar(result.message ?: "削除に失敗しました"))
                }
                is Resource.Loading -> {
                    // Loading state already handled above
                }
            }
        }
    }

    fun clearForm() {
        _uiState.value = DiaryCreateEditUiState()
    }
}