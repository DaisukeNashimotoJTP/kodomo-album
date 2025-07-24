package com.example.kodomo_album.presentation.media

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kodomo_album.core.util.Resource
import com.example.kodomo_album.core.util.UiEvent
import com.example.kodomo_album.domain.model.MediaMetadata
import com.example.kodomo_album.domain.model.MediaType
import com.example.kodomo_album.domain.usecase.media.UploadMediaUseCase
import com.example.kodomo_album.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MediaUploadViewModel @Inject constructor(
    private val uploadMediaUseCase: UploadMediaUseCase
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(MediaUploadUiState())
    val uiState: StateFlow<MediaUploadUiState> = _uiState.asStateFlow()

    fun onImageSelected(uri: Uri) {
        _uiState.value = _uiState.value.copy(
            selectedImageUri = uri,
            mediaType = MediaType.PHOTO
        )
    }

    fun onVideoSelected(uri: Uri) {
        _uiState.value = _uiState.value.copy(
            selectedImageUri = uri,
            mediaType = MediaType.VIDEO
        )
    }

    fun onCaptionChanged(caption: String) {
        _uiState.value = _uiState.value.copy(caption = caption)
    }

    fun onChildSelected(childId: String) {
        _uiState.value = _uiState.value.copy(selectedChildId = childId)
    }

    fun uploadMedia(file: File) {
        val currentState = _uiState.value
        
        if (currentState.selectedChildId.isEmpty()) {
            sendUiEvent(UiEvent.ShowSnackbar("子どもを選択してください"))
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true)

            val metadata = MediaMetadata(
                childId = currentState.selectedChildId,
                caption = currentState.caption.takeIf { it.isNotBlank() },
                type = currentState.mediaType
            )

            when (val result = uploadMediaUseCase(file, metadata)) {
                is Resource.Success -> {
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        uploadSuccess = true
                    )
                    sendUiEvent(UiEvent.ShowSnackbar("アップロードが完了しました"))
                }
                is Resource.Error -> {
                    _uiState.value = currentState.copy(isLoading = false)
                    sendUiEvent(UiEvent.ShowSnackbar(result.message ?: "アップロードに失敗しました"))
                }
                is Resource.Loading -> {
                    // Already handled above
                }
            }
        }
    }

    fun resetUploadState() {
        _uiState.value = MediaUploadUiState()
    }

    fun clearSelectedMedia() {
        _uiState.value = _uiState.value.copy(
            selectedImageUri = null,
            caption = "",
            mediaType = MediaType.PHOTO
        )
    }
}

data class MediaUploadUiState(
    val selectedImageUri: Uri? = null,
    val caption: String = "",
    val selectedChildId: String = "",
    val mediaType: MediaType = MediaType.PHOTO,
    val isLoading: Boolean = false,
    val uploadSuccess: Boolean = false
)