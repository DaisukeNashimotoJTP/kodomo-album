package com.example.kodomo_album.domain.usecase.diary

import com.example.kodomo_album.domain.repository.DiaryRepository
import com.example.kodomo_album.core.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class DeleteDiaryUseCaseTest {
    
    private lateinit var diaryRepository: DiaryRepository
    private lateinit var deleteDiaryUseCase: DeleteDiaryUseCase
    
    @BeforeEach
    fun setUp() {
        diaryRepository = mockk()
        deleteDiaryUseCase = DeleteDiaryUseCase(diaryRepository)
    }
    
    @Test
    fun `invoke deletes diary successfully`() = runTest {
        // Given
        val diaryId = "diary123"
        
        coEvery { diaryRepository.deleteDiary(diaryId) } returns Resource.Success(Unit)
        
        // When
        val result = deleteDiaryUseCase.invoke(diaryId)
        
        // Then
        assertTrue(result is Resource.Success)
        coVerify { diaryRepository.deleteDiary(diaryId) }
    }
    
    @Test
    fun `invoke returns error when repository fails`() = runTest {
        // Given
        val diaryId = "diary123"
        val errorMessage = "削除に失敗しました"
        
        coEvery { diaryRepository.deleteDiary(diaryId) } returns Resource.Error(errorMessage)
        
        // When
        val result = deleteDiaryUseCase.invoke(diaryId)
        
        // Then
        assertTrue(result is Resource.Error)
        assertEquals(errorMessage, (result as Resource.Error).message)
    }
}