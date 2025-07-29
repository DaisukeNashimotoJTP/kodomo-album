package com.example.kodomo_album.domain.repository

import com.example.kodomo_album.domain.model.Diary
import com.example.kodomo_album.core.util.Resource
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface DiaryRepository {
    suspend fun createDiary(diary: Diary): Resource<Diary>
    suspend fun getDiaries(childId: String): Resource<List<Diary>>
    fun getDiariesFlow(childId: String): Flow<List<Diary>>
    suspend fun getDiariesByDateRange(childId: String, startDate: LocalDate, endDate: LocalDate): Resource<List<Diary>>
    suspend fun getDiaryById(diaryId: String): Resource<Diary>
    suspend fun updateDiary(diary: Diary): Resource<Diary>
    suspend fun deleteDiary(diaryId: String): Resource<Unit>
    suspend fun searchDiaries(childId: String, keyword: String): Resource<List<Diary>>
}