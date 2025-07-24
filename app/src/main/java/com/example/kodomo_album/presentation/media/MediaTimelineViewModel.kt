package com.example.kodomo_album.presentation.media

import androidx.lifecycle.viewModelScope
import com.example.kodomo_album.core.util.UiEvent
import com.example.kodomo_album.domain.model.Media
import com.example.kodomo_album.domain.model.MediaType
import com.example.kodomo_album.domain.usecase.media.GetMediaListUseCase
import com.example.kodomo_album.domain.usecase.media.DeleteMediaUseCase
import com.example.kodomo_album.core.util.Resource
import com.example.kodomo_album.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MediaTimelineViewModel @Inject constructor(
    private val getMediaListUseCase: GetMediaListUseCase,
    private val deleteMediaUseCase: DeleteMediaUseCase
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(MediaTimelineUiState())
    val uiState: StateFlow<MediaTimelineUiState> = _uiState.asStateFlow()

    fun loadMediaList(childId: String) {
        if (childId.isEmpty()) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            getMediaListUseCase(childId, _uiState.value.dateRange)
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "メディア読み込みに失敗しました"
                    )
                    sendUiEvent(UiEvent.ShowSnackbar("メディア読み込みに失敗しました"))
                }
                .collect { mediaList ->
                    val filteredList = when (_uiState.value.selectedMediaType) {
                        MediaType.PHOTO -> mediaList.filter { it.type == MediaType.PHOTO || it.type == MediaType.ECHO }
                        MediaType.VIDEO -> mediaList.filter { it.type == MediaType.VIDEO }
                        MediaType.ECHO -> mediaList.filter { it.type == MediaType.ECHO }
                        else -> mediaList
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        mediaList = filteredList.sortedByDescending { it.takenAt },
                        error = null
                    )
                }
        }
    }

    fun onDateFilterChanged(startDate: LocalDate?, endDate: LocalDate?) {
        val dateRange = if (startDate != null && endDate != null) {
            Pair(startDate, endDate)
        } else null
        
        _uiState.value = _uiState.value.copy(dateRange = dateRange)
        
        // 現在の子どもIDで再読み込み
        val currentChildId = _uiState.value.currentChildId
        if (currentChildId.isNotEmpty()) {
            loadMediaList(currentChildId)
        }
    }

    fun onMediaTypeFilterChanged(mediaType: MediaType?) {
        _uiState.value = _uiState.value.copy(selectedMediaType = mediaType)
        
        // 現在の子どもIDで再読み込み
        val currentChildId = _uiState.value.currentChildId
        if (currentChildId.isNotEmpty()) {
            loadMediaList(currentChildId)
        }
    }

    fun onChildChanged(childId: String) {
        _uiState.value = _uiState.value.copy(currentChildId = childId)
        loadMediaList(childId)
    }

    fun onMediaSelected(media: Media) {
        _uiState.value = _uiState.value.copy(selectedMedia = media)
    }

    fun clearSelectedMedia() {
        _uiState.value = _uiState.value.copy(selectedMedia = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun refreshMediaList() {
        val currentChildId = _uiState.value.currentChildId
        if (currentChildId.isNotEmpty()) {
            loadMediaList(currentChildId)
        }
    }

    fun deleteMedia(mediaId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            when (val result = deleteMediaUseCase(mediaId)) {
                is Resource.Success -> {
                    sendUiEvent(UiEvent.ShowSnackbar("メディアを削除しました"))
                    refreshMediaList()
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    sendUiEvent(UiEvent.ShowSnackbar(result.message ?: "削除に失敗しました"))
                }
                is Resource.Loading -> {
                    // Already handled above
                }
            }
        }
    }
}

data class MediaTimelineUiState(
    val isLoading: Boolean = false,
    val mediaList: List<Media> = emptyList(),
    val currentChildId: String = "",
    val selectedMedia: Media? = null,
    val selectedMediaType: MediaType? = null,
    val dateRange: Pair<LocalDate, LocalDate>? = null,
    val error: String? = null
)