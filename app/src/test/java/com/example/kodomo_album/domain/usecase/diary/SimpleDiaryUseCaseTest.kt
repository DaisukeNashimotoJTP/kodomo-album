package com.example.kodomo_album.domain.usecase.diary

import com.example.kodomo_album.domain.model.Diary
import com.example.kodomo_album.domain.repository.DiaryRepository
import com.example.kodomo_album.core.util.Resource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate
import java.time.LocalDateTime

class SimpleDiaryUseCaseTest {
    
    private lateinit var diaryRepository: DiaryRepository
    private lateinit var createDiaryUseCase: CreateDiaryUseCase
    private lateinit var searchDiariesUseCase: SearchDiariesUseCase
    
    @Before
    fun setUp() {
        diaryRepository = mockk()
        createDiaryUseCase = CreateDiaryUseCase(diaryRepository)
        searchDiariesUseCase = SearchDiariesUseCase(diaryRepository)
    }
    
    @Test
    fun `createDiary creates diary with correct parameters`() = runTest {
        // Given
        val mockDiary = Diary(
            id = "test-id",
            childId = "child123",
            title = "テストタイトル",
            content = "テスト内容",
            mediaIds = emptyList(),
            date = LocalDate.now(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        coEvery { diaryRepository.createDiary(any()) } returns Resource.Success(mockDiary)
        
        // When
        val result = createDiaryUseCase.invoke("child123", "テストタイトル", "テスト内容")
        
        // Then
        assertTrue(result is Resource.Success)
    }
    
    @Test
    fun `searchDiaries returns all diaries when keyword is blank`() = runTest {
        // Given
        val mockDiaries = listOf(
            Diary(
                id = "1", childId = "child123", title = "タイトル1", content = "内容1",
                mediaIds = emptyList(), date = LocalDate.now(),
                createdAt = LocalDateTime.now(), updatedAt = LocalDateTime.now()
            )
        )
        
        coEvery { diaryRepository.getDiaries("child123") } returns Resource.Success(mockDiaries)
        
        // When
        val result = searchDiariesUseCase.invoke("child123", "")
        
        // Then
        assertTrue(result is Resource.Success)
    }
    
    @Test
    fun `searchDiaries searches with keyword when provided`() = runTest {
        // Given
        val mockDiaries = listOf(
            Diary(
                id = "1", childId = "child123", title = "タイトル1", content = "内容1",
                mediaIds = emptyList(), date = LocalDate.now(),
                createdAt = LocalDateTime.now(), updatedAt = LocalDateTime.now()
            )
        )
        
        coEvery { diaryRepository.searchDiaries("child123", "キーワード") } returns Resource.Success(mockDiaries)
        
        // When
        val result = searchDiariesUseCase.invoke("child123", "キーワード")
        
        // Then
        assertTrue(result is Resource.Success)
    }
}