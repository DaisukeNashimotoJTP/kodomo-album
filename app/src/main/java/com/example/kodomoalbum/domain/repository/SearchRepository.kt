package com.example.kodomoalbum.domain.repository

import com.example.kodomoalbum.data.model.SearchFilter
import com.example.kodomoalbum.data.model.SearchHistory
import com.example.kodomoalbum.data.model.SearchResult
import kotlinx.coroutines.flow.Flow

interface SearchRepository {
    
    suspend fun searchContent(
        query: String,
        filter: SearchFilter = SearchFilter()
    ): Flow<List<SearchResult>>
    
    suspend fun getSearchHistory(): Flow<List<SearchHistory>>
    
    suspend fun saveSearchHistory(query: String, resultsCount: Int)
    
    suspend fun clearSearchHistory()
    
    suspend fun deleteSearchHistory(id: String)
}