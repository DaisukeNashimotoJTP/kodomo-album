package com.example.kodomo_album.presentation.child

import androidx.lifecycle.viewModelScope
import com.example.kodomo_album.core.util.UiEvent
import com.example.kodomo_album.domain.model.Child
import com.example.kodomo_album.domain.model.Gender
import com.example.kodomo_album.domain.usecase.child.CreateChildUseCase
import com.example.kodomo_album.domain.usecase.child.DeleteChildUseCase
import com.example.kodomo_album.domain.usecase.child.GetChildrenUseCase
import com.example.kodomo_album.domain.usecase.child.UpdateChildUseCase
import com.example.kodomo_album.domain.usecase.child.GetChildByIdUseCase
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
class ChildManagementViewModel @Inject constructor(
    private val createChildUseCase: CreateChildUseCase,
    private val getChildrenUseCase: GetChildrenUseCase,
    private val getChildByIdUseCase: GetChildByIdUseCase,
    private val updateChildUseCase: UpdateChildUseCase,
    private val deleteChildUseCase: DeleteChildUseCase
) : BaseViewModel() {

    private val _children = MutableStateFlow<List<Child>>(emptyList())
    val children: StateFlow<List<Child>> = _children.asStateFlow()

    private val _selectedChild = MutableStateFlow<Child?>(null)
    val selectedChild: StateFlow<Child?> = _selectedChild.asStateFlow()

    fun loadChildren(userId: String) {
        viewModelScope.launch {
            getChildrenUseCase.getChildrenFlow(userId)
                .catch { e ->
                    sendUiEvent(UiEvent.ShowSnackbar("子どもの読み込みに失敗しました: ${e.message}"))
                }
                .collect { childrenList ->
                    _children.value = childrenList
                }
        }
    }

    fun createChild(
        userId: String,
        name: String,
        birthDate: LocalDate,
        gender: Gender,
        profileImageUrl: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            
            val child = Child(
                id = "",
                userId = userId,
                name = name,
                birthDate = birthDate,
                gender = gender,
                profileImageUrl = profileImageUrl
            )

            createChildUseCase(child)
                .onSuccess {
                    sendUiEvent(UiEvent.ShowSnackbar("子どもを追加しました"))
                }
                .onFailure { e ->
                    sendUiEvent(UiEvent.ShowSnackbar("子どもの追加に失敗しました: ${e.message}"))
                }
            
            _isLoading.value = false
        }
    }

    fun updateChild(child: Child) {
        viewModelScope.launch {
            _isLoading.value = true
            
            updateChildUseCase(child)
                .onSuccess {
                    sendUiEvent(UiEvent.ShowSnackbar("子どもの情報を更新しました"))
                }
                .onFailure { e ->
                    sendUiEvent(UiEvent.ShowSnackbar("更新に失敗しました: ${e.message}"))
                }
            
            _isLoading.value = false
        }
    }

    fun deleteChild(childId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            
            deleteChildUseCase(childId)
                .onSuccess {
                    sendUiEvent(UiEvent.ShowSnackbar("子どもを削除しました"))
                }
                .onFailure { e ->
                    sendUiEvent(UiEvent.ShowSnackbar("削除に失敗しました: ${e.message}"))
                }
            
            _isLoading.value = false
        }
    }

    fun selectChild(child: Child?) {
        _selectedChild.value = child
    }

    fun getChildById(childId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val child = getChildByIdUseCase(childId)
                _selectedChild.value = child
            } catch (e: Exception) {
                sendUiEvent(UiEvent.ShowSnackbar("子どもの情報取得に失敗しました: ${e.message}"))
            }
            _isLoading.value = false
        }
    }
}