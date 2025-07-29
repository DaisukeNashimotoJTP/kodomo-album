package com.example.kodomo_album.presentation.growth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kodomo_album.core.util.Resource
import com.example.kodomo_album.domain.model.GrowthChartData
import com.example.kodomo_album.domain.model.GrowthType
import com.example.kodomo_album.domain.model.GrowthPeriod
import com.example.kodomo_album.domain.usecase.growth.GetGrowthChartUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GrowthChartViewModel @Inject constructor(
    private val getGrowthChartUseCase: GetGrowthChartUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(GrowthChartState())
    val state: StateFlow<GrowthChartState> = _state.asStateFlow()

    private var currentChildId: String = ""

    fun loadGrowthChart(childId: String) {
        currentChildId = childId
        fetchGrowthChart()
    }

    fun setGrowthType(type: GrowthType) {
        _state.value = _state.value.copy(selectedType = type)
        fetchGrowthChart()
    }

    fun setGrowthPeriod(period: GrowthPeriod) {
        _state.value = _state.value.copy(selectedPeriod = period)
        fetchGrowthChart()
    }

    private fun fetchGrowthChart() {
        if (currentChildId.isEmpty()) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            when (val result = getGrowthChartUseCase(
                childId = currentChildId,
                type = _state.value.selectedType,
                period = _state.value.selectedPeriod
            )) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        chartData = result.data,
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

data class GrowthChartState(
    val isLoading: Boolean = false,
    val chartData: GrowthChartData? = null,
    val selectedType: GrowthType = GrowthType.ALL,
    val selectedPeriod: GrowthPeriod = GrowthPeriod.ALL,
    val error: String? = null
)