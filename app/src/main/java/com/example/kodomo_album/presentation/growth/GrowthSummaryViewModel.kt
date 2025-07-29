package com.example.kodomo_album.presentation.growth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kodomo_album.core.util.Resource
import com.example.kodomo_album.domain.model.GrowthSummary
import com.example.kodomo_album.domain.model.GrowthPeriod
import com.example.kodomo_album.domain.usecase.growth.GetGrowthSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GrowthSummaryViewModel @Inject constructor(
    private val getGrowthSummaryUseCase: GetGrowthSummaryUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(GrowthSummaryState())
    val state: StateFlow<GrowthSummaryState> = _state.asStateFlow()

    private var currentChildId: String = ""

    fun loadGrowthSummary(childId: String) {
        currentChildId = childId
        fetchGrowthSummary()
    }

    fun setPeriod(period: GrowthPeriod) {
        _state.value = _state.value.copy(selectedPeriod = period)
        fetchGrowthSummary()
    }

    private fun fetchGrowthSummary() {
        if (currentChildId.isEmpty()) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            when (val result = getGrowthSummaryUseCase(
                childId = currentChildId,
                period = _state.value.selectedPeriod
            )) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        summary = result.data,
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

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}

data class GrowthSummaryState(
    val isLoading: Boolean = false,
    val summary: GrowthSummary? = null,
    val selectedPeriod: GrowthPeriod = GrowthPeriod.MONTH,
    val error: String? = null
)