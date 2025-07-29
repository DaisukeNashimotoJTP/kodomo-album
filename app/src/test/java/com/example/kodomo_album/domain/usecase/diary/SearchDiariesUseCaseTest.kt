package com.example.kodomo_album.domain.usecase.diary

import com.example.kodomo_album.domain.model.Diary
import com.example.kodomo_album.domain.repository.DiaryRepository
import com.example.kodomo_album.core.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class SearchDiariesUseCaseTest {
    
    private lateinit var diaryRepository: DiaryRepository
    private lateinit var searchDiariesUseCase: SearchDiariesUseCase
    
    @BeforeEach
    fun setUp() {
        diaryRepository = mockk()
        searchDiariesUseCase = SearchDiariesUseCase(diaryRepository)
    }
    
    @Test
    fun `invoke searches diaries with keyword`() = runTest {
        // Given
        val childId = "child123"
        val keyword = "笑顔"
        val expectedDiaries = listOf(mockk<Diary>())
        
        coEvery { diaryRepository.searchDiaries(childId, keyword) } returns Resource.Success(expectedDiaries)
        
        // When
        val result = searchDiariesUseCase.invoke(childId, keyword)
        
        // Then
        assertTrue(result is Resource.Success)
        assertEquals(expectedDiaries, (result as Resource.Success).data)
        coVerify { diaryRepository.searchDiaries(childId, keyword) }
    }
    
    @Test
    fun `invoke returns all diaries when keyword is blank`() = runTest {
        // Given
        val childId = "child123"
        val keyword = "  "
        val expectedDiaries = listOf(mockk<Diary>())
        
        coEvery { diaryRepository.getDiaries(childId) } returns Resource.Success(expectedDiaries)
        
        // When
        val result = searchDiariesUseCase.invoke(childId, keyword)
        
        // Then
        assertTrue(result is Resource.Success)
        assertEquals(expectedDiaries, (result as Resource.Success).data)
        coVerify { diaryRepository.getDiaries(childId) }
        coVerify(exactly = 0) { diaryRepository.searchDiaries(any(), any()) }
    }
    
    @Test
    fun `invoke trims keyword before search`() = runTest {
        // Given
        val childId = "child123"
        val keyword = "  笑顔  "
        val trimmedKeyword = "笑顔"
        val expectedDiaries = listOf(mockk<Diary>())
        
        coEvery { diaryRepository.searchDiaries(childId, trimmedKeyword) } returns Resource.Success(expectedDiaries)
        
        // When
        val result = searchDiariesUseCase.invoke(childId, keyword)
        
        // Then
        assertTrue(result is Resource.Success)
        coVerify { diaryRepository.searchDiaries(childId, trimmedKeyword) }
    }
    
    @Test
    fun `invoke returns error when repository fails`() = runTest {
        // Given
        val childId = "child123"
        val keyword = "笑顔"
        val errorMessage = "検索に失敗しました"
        
        coEvery { diaryRepository.searchDiaries(childId, keyword) } returns Resource.Error(errorMessage)
        
        // When
        val result = searchDiariesUseCase.invoke(childId, keyword)
        
        // Then
        assertTrue(result is Resource.Error)
        assertEquals(errorMessage, (result as Resource.Error).message)
    }
}