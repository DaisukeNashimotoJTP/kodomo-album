package com.example.kodomo_album.presentation.growth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kodomo_album.core.util.Resource
import com.example.kodomo_album.domain.model.GrowthRecord
import com.example.kodomo_album.domain.usecase.growth.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class GrowthRecordViewModel @Inject constructor(
    private val recordGrowthUseCase: RecordGrowthUseCase,
    private val getGrowthHistoryUseCase: GetGrowthHistoryUseCase,
    private val updateGrowthRecordUseCase: UpdateGrowthRecordUseCase,
    private val deleteGrowthRecordUseCase: DeleteGrowthRecordUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(GrowthRecordState())
    val state: StateFlow<GrowthRecordState> = _state.asStateFlow()

    fun updateHeight(height: String) {
        _state.value = _state.value.copy(height = height)
    }

    fun updateWeight(weight: String) {
        _state.value = _state.value.copy(weight = weight)
    }

    fun updateHeadCircumference(headCircumference: String) {
        _state.value = _state.value.copy(headCircumference = headCircumference)
    }

    fun updateNotes(notes: String) {
        _state.value = _state.value.copy(notes = notes)
    }

    fun updateRecordedDate(date: LocalDate) {
        _state.value = _state.value.copy(recordedDate = date)
    }

    fun saveGrowthRecord(childId: String) {
        if (childId.isEmpty()) {
            _state.value = _state.value.copy(error = "子どもIDが必要です")
            return
        }

        val height = _state.value.height.toDoubleOrNull()
        val weight = _state.value.weight.toDoubleOrNull()
        val headCircumference = _state.value.headCircumference.toDoubleOrNull()

        if (height == null && weight == null && headCircumference == null) {
            _state.value = _state.value.copy(
                error = "身長、体重、頭囲のうち少なくとも1つは入力してください"
            )
            return
        }

        val growthRecord = GrowthRecord(
            id = _state.value.editingRecord?.id ?: "",
            childId = childId,
            height = height,
            weight = weight,
            headCircumference = headCircumference,
            recordedAt = _state.value.recordedDate,
            notes = _state.value.notes.takeIf { it.isNotBlank() }
        )

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            val result = if (_state.value.editingRecord != null) {
                updateGrowthRecordUseCase(growthRecord)
            } else {
                recordGrowthUseCase(growthRecord)
            }

            when (result) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isSaved = true,
                        error = null
                    )
                    clearForm()
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is Resource.Loading -> {
                    // Loadingはすでに設定済み
                }
            }
        }
    }

    fun loadGrowthHistory(childId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            getGrowthHistoryUseCase(childId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            growthRecords = result.data ?: emptyList(),
                            error = null
                        )
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                    is Resource.Loading -> {
                        // Loadingはすでに設定済み
                    }
                }
            }
        }
    }

    fun editGrowthRecord(record: GrowthRecord) {
        _state.value = _state.value.copy(
            editingRecord = record,
            height = record.height?.toString() ?: "",
            weight = record.weight?.toString() ?: "",
            headCircumference = record.headCircumference?.toString() ?: "",
            notes = record.notes ?: "",
            recordedDate = record.recordedAt
        )
    }

    fun deleteGrowthRecord(recordId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            when (val result = deleteGrowthRecordUseCase(recordId)) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = null
                    )
                    // リストを再読み込み
                    val currentChildId = _state.value.growthRecords.firstOrNull()?.childId
                    if (currentChildId != null) {
                        loadGrowthHistory(currentChildId)
                    }
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is Resource.Loading -> {
                    // Loadingはすでに設定済み
                }
            }
        }
    }

    fun clearForm() {
        _state.value = _state.value.copy(
            height = "",
            weight = "",
            headCircumference = "",
            notes = "",
            recordedDate = LocalDate.now(),
            editingRecord = null,
            isSaved = false
        )
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}

data class GrowthRecordState(
    val isLoading: Boolean = false,
    val height: String = "",
    val weight: String = "",
    val headCircumference: String = "",
    val notes: String = "",
    val recordedDate: LocalDate = LocalDate.now(),
    val growthRecords: List<GrowthRecord> = emptyList(),
    val editingRecord: GrowthRecord? = null,
    val isSaved: Boolean = false,
    val error: String? = null
)