package com.example.kodomo_album.domain.usecase.diary

import com.example.kodomo_album.domain.model.Diary
import com.example.kodomo_album.domain.repository.DiaryRepository
import com.example.kodomo_album.core.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class GetDiariesUseCaseTest {
    
    private lateinit var diaryRepository: DiaryRepository
    private lateinit var getDiariesUseCase: GetDiariesUseCase
    
    @BeforeEach
    fun setUp() {
        diaryRepository = mockk()
        getDiariesUseCase = GetDiariesUseCase(diaryRepository)
    }
    
    @Test
    fun `invoke returns diaries successfully`() = runTest {
        // Given
        val childId = "child123"
        val expectedDiaries = listOf(
            mockk<Diary>(),
            mockk<Diary>()
        )
        
        coEvery { diaryRepository.getDiaries(childId) } returns Resource.Success(expectedDiaries)
        
        // When
        val result = getDiariesUseCase.invoke(childId)
        
        // Then
        assertTrue(result is Resource.Success)
        assertEquals(expectedDiaries, (result as Resource.Success).data)
        coVerify { diaryRepository.getDiaries(childId) }
    }
    
    @Test
    fun `invoke returns error when repository fails`() = runTest {
        // Given
        val childId = "child123"
        val errorMessage = "データベースエラー"
        
        coEvery { diaryRepository.getDiaries(childId) } returns Resource.Error(errorMessage)
        
        // When
        val result = getDiariesUseCase.invoke(childId)
        
        // Then
        assertTrue(result is Resource.Error)
        assertEquals(errorMessage, (result as Resource.Error).message)
    }
    
    @Test
    fun `getFlow returns diary flow`() = runTest {
        // Given
        val childId = "child123"
        val expectedDiaries = listOf(mockk<Diary>())
        val expectedFlow = flowOf(expectedDiaries)
        
        every { diaryRepository.getDiariesFlow(childId) } returns expectedFlow
        
        // When
        val resultFlow = getDiariesUseCase.getFlow(childId)
        
        // Then
        assertEquals(expectedFlow, resultFlow)
        coVerify { diaryRepository.getDiariesFlow(childId) }
    }
}