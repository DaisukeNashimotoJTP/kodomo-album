package com.example.kodomoalbum.data.repository

import com.example.kodomo_album.data.local.dao.DiaryDao
import com.example.kodomo_album.data.local.dao.EventDao
import com.example.kodomo_album.data.local.dao.MediaDao
import com.example.kodomo_album.data.local.dao.MilestoneDao
import com.example.kodomoalbum.data.model.SearchFilter
import com.example.kodomoalbum.data.model.SearchHistory
import com.example.kodomoalbum.data.model.SearchResult
import com.example.kodomoalbum.data.model.SearchResultType
import com.example.kodomoalbum.domain.repository.SearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepositoryImpl @Inject constructor(
    private val diaryDao: DiaryDao,
    private val mediaDao: MediaDao,
    private val eventDao: EventDao,
    private val milestoneDao: MilestoneDao
) : SearchRepository {
    
    private val searchHistoryList = mutableListOf<SearchHistory>()
    
    override suspend fun searchContent(
        query: String,
        filter: SearchFilter
    ): Flow<List<SearchResult>> = flow {
        val results = mutableListOf<SearchResult>()
        
        // 日記検索
        if (filter.types.isEmpty() || filter.types.contains(SearchResultType.DIARY)) {
            val diaries = diaryDao.searchDiaries("%$query%")
            diaries.forEach { diary ->
                if (isWithinDateRange(diary.date.atStartOfDay(), filter.dateRange) &&
                    (filter.childIds.isEmpty() || filter.childIds.contains(diary.childId))) {
                    results.add(
                        SearchResult(
                            id = diary.id,
                            type = SearchResultType.DIARY,
                            title = diary.title,
                            content = diary.content,
                            date = diary.date.atStartOfDay(),
                            childId = diary.childId
                        )
                    )
                }
            }
        }
        
        // メディア検索
        if (filter.types.isEmpty() || filter.types.contains(SearchResultType.MEDIA)) {
            val mediaList = mediaDao.searchMedia("%$query%")
            mediaList.forEach { media ->
                if (isWithinDateRange(media.takenAt, filter.dateRange) &&
                    (filter.childIds.isEmpty() || filter.childIds.contains(media.childId))) {
                    results.add(
                        SearchResult(
                            id = media.id,
                            type = SearchResultType.MEDIA,
                            title = media.caption ?: "写真・動画",
                            content = media.caption ?: "",
                            date = media.takenAt,
                            thumbnailUrl = media.thumbnailUrl,
                            childId = media.childId
                        )
                    )
                }
            }
        }
        
        // イベント検索
        if (filter.types.isEmpty() || filter.types.contains(SearchResultType.EVENT)) {
            val events = eventDao.searchEvents("%$query%")
            events.forEach { event ->
                if (isWithinDateRange(event.eventDate.atStartOfDay(), filter.dateRange) &&
                    (filter.childIds.isEmpty() || filter.childIds.contains(event.childId))) {
                    results.add(
                        SearchResult(
                            id = event.id,
                            type = SearchResultType.EVENT,
                            title = event.title,
                            content = event.description,
                            date = event.eventDate.atStartOfDay(),
                            childId = event.childId
                        )
                    )
                }
            }
        }
        
        // マイルストーン検索
        if (filter.types.isEmpty() || filter.types.contains(SearchResultType.MILESTONE)) {
            val milestones = milestoneDao.searchMilestones("%$query%")
            milestones.forEach { milestone ->
                if (isWithinDateRange(milestone.achievedAt.atStartOfDay(), filter.dateRange) &&
                    (filter.childIds.isEmpty() || filter.childIds.contains(milestone.childId))) {
                    results.add(
                        SearchResult(
                            id = milestone.id,
                            type = SearchResultType.MILESTONE,
                            title = milestone.title,
                            content = milestone.description,
                            date = milestone.achievedAt.atStartOfDay(),
                            childId = milestone.childId
                        )
                    )
                }
            }
        }
        
        // 日付順でソート
        emit(results.sortedByDescending { it.date })
    }
    
    override suspend fun getSearchHistory(): Flow<List<SearchHistory>> = flow {
        emit(searchHistoryList.sortedByDescending { it.searchedAt })
    }
    
    override suspend fun saveSearchHistory(query: String, resultsCount: Int) {
        val history = SearchHistory(
            id = UUID.randomUUID().toString(),
            query = query,
            searchedAt = LocalDateTime.now(),
            resultsCount = resultsCount
        )
        searchHistoryList.add(history)
        
        // 最大20件まで保持
        if (searchHistoryList.size > 20) {
            searchHistoryList.removeAt(0)
        }
    }
    
    override suspend fun clearSearchHistory() {
        searchHistoryList.clear()
    }
    
    override suspend fun deleteSearchHistory(id: String) {
        searchHistoryList.removeAll { it.id == id }
    }
    
    private fun isWithinDateRange(
        date: LocalDateTime,
        dateRange: com.example.kodomoalbum.data.model.DateRange?
    ): Boolean {
        if (dateRange == null) return true
        return date >= dateRange.startDate.atStartOfDay() && 
               date <= dateRange.endDate.atTime(23, 59, 59)
    }
}