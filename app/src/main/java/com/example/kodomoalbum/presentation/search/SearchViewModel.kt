package com.example.kodomoalbum.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kodomoalbum.data.model.SearchFilter
import com.example.kodomoalbum.data.model.SearchHistory
import com.example.kodomoalbum.data.model.SearchResult
import com.example.kodomoalbum.data.model.SearchResultType
import com.example.kodomoalbum.domain.usecase.SearchUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchUseCase: SearchUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    
    private val _searchResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResults: StateFlow<List<SearchResult>> = _searchResults.asStateFlow()
    
    private val _searchHistory = MutableStateFlow<List<SearchHistory>>(emptyList())
    val searchHistory: StateFlow<List<SearchHistory>> = _searchHistory.asStateFlow()
    
    init {
        loadSearchHistory()
    }
    
    fun search(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            searchUseCase.searchContent(query, _uiState.value.searchFilter)
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "検索中にエラーが発生しました"
                    )
                }
                .collect { results ->
                    _searchResults.value = results
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        query = query
                    )
                    
                    // 検索履歴に保存
                    searchUseCase.saveSearchHistory(query, results.size)
                    loadSearchHistory()
                }
        }
    }
    
    fun updateSearchFilter(filter: SearchFilter) {
        _uiState.value = _uiState.value.copy(searchFilter = filter)
        
        // フィルターが変更されたら再検索
        if (_uiState.value.query.isNotBlank()) {
            search(_uiState.value.query)
        }
    }
    
    fun clearSearch() {
        _searchResults.value = emptyList()
        _uiState.value = _uiState.value.copy(query = "", error = null)
    }
    
    fun deleteSearchHistory(id: String) {
        viewModelScope.launch {
            searchUseCase.deleteSearchHistory(id)
            loadSearchHistory()
        }
    }
    
    fun clearSearchHistory() {
        viewModelScope.launch {
            searchUseCase.clearSearchHistory()
            loadSearchHistory()
        }
    }
    
    private fun loadSearchHistory() {
        viewModelScope.launch {
            searchUseCase.getSearchHistory()
                .catch { exception ->
                    // エラーハンドリング
                }
                .collect { history ->
                    _searchHistory.value = history
                }
        }
    }
    
    fun toggleFilterType(type: SearchResultType) {
        val currentFilter = _uiState.value.searchFilter
        val newTypes = if (currentFilter.types.contains(type)) {
            currentFilter.types - type
        } else {
            currentFilter.types + type
        }
        updateSearchFilter(currentFilter.copy(types = newTypes))
    }
}

data class SearchUiState(
    val isLoading: Boolean = false,
    val query: String = "",
    val searchFilter: SearchFilter = SearchFilter(),
    val error: String? = null
)