package com.example.kodomoalbum.domain.usecase

import com.example.kodomoalbum.data.model.SearchFilter
import com.example.kodomoalbum.data.model.SearchHistory
import com.example.kodomoalbum.data.model.SearchResult
import com.example.kodomoalbum.domain.repository.SearchRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchUseCase @Inject constructor(
    private val searchRepository: SearchRepository
) {
    
    suspend fun searchContent(
        query: String,
        filter: SearchFilter = SearchFilter()
    ): Flow<List<SearchResult>> {
        return searchRepository.searchContent(query, filter)
    }
    
    suspend fun getSearchHistory(): Flow<List<SearchHistory>> {
        return searchRepository.getSearchHistory()
    }
    
    suspend fun saveSearchHistory(query: String, resultsCount: Int) {
        searchRepository.saveSearchHistory(query, resultsCount)
    }
    
    suspend fun clearSearchHistory() {
        searchRepository.clearSearchHistory()
    }
    
    suspend fun deleteSearchHistory(id: String) {
        searchRepository.deleteSearchHistory(id)
    }
}