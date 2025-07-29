package com.example.kodomo_album.presentation.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kodomo_album.domain.model.Diary
import com.example.kodomo_album.domain.usecase.diary.GetDiariesUseCase
import com.example.kodomo_album.domain.usecase.diary.SearchDiariesUseCase
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

data class DiaryListUiState(
    val diaries: List<Diary> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val selectedDate: LocalDate? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class DiaryListViewModel @Inject constructor(
    private val getDiariesUseCase: GetDiariesUseCase,
    private val searchDiariesUseCase: SearchDiariesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiaryListUiState())
    val uiState: StateFlow<DiaryListUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun loadDiaries(childId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            // Flowを使用してリアルタイム更新を受け取る
            getDiariesUseCase.getFlow(childId).collect { diaries ->
                _uiState.value = _uiState.value.copy(
                    diaries = diaries,
                    isLoading = false
                )
            }
        }
    }

    fun searchDiaries(childId: String, query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            isSearching = query.isNotBlank()
        )

        if (query.isBlank()) {
            loadDiaries(childId)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            when (val result = searchDiariesUseCase(childId, query)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        diaries = result.data ?: emptyList(),
                        isLoading = false
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                    _uiEvent.send(UiEvent.ShowSnackbar(result.message ?: "検索に失敗しました"))
                }
                is Resource.Loading -> {
                    // Loading state already handled above
                }
            }
        }
    }

    fun clearSearch(childId: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            isSearching = false
        )
        loadDiaries(childId)
    }

    fun refreshDiaries(childId: String) {
        loadDiaries(childId)
    }

    fun filterByDate(childId: String, date: LocalDate?) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
        
        if (date == null) {
            loadDiaries(childId)
        } else {
            // 日付フィルタリングは既存のリストをフィルタ
            val filteredDiaries = _uiState.value.diaries.filter { diary ->
                diary.date == date
            }
            _uiState.value = _uiState.value.copy(diaries = filteredDiaries)
        }
    }

    fun clearDateFilter(childId: String) {
        filterByDate(childId, null)
    }
}