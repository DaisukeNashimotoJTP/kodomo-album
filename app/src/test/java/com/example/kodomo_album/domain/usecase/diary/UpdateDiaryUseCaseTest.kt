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
import java.time.LocalDate
import java.time.LocalDateTime

class UpdateDiaryUseCaseTest {
    
    private lateinit var diaryRepository: DiaryRepository
    private lateinit var updateDiaryUseCase: UpdateDiaryUseCase
    
    @BeforeEach
    fun setUp() {
        diaryRepository = mockk()
        updateDiaryUseCase = UpdateDiaryUseCase(diaryRepository)
    }
    
    @Test
    fun `invoke updates diary with new values`() = runTest {
        // Given
        val originalDiary = Diary(
            id = "diary123",
            childId = "child123",
            title = "元のタイトル",
            content = "元の内容",
            mediaIds = listOf("media1"),
            date = LocalDate.now(),
            createdAt = LocalDateTime.now().minusHours(1),
            updatedAt = LocalDateTime.now().minusHours(1)
        )
        
        val newTitle = "新しいタイトル"
        val newContent = "新しい内容"
        val newMediaIds = listOf("media1", "media2")
        
        coEvery { diaryRepository.updateDiary(any()) } answers { Resource.Success(firstArg()) }
        
        // When
        val result = updateDiaryUseCase.invoke(
            diary = originalDiary,
            newTitle = newTitle,
            newContent = newContent,
            newMediaIds = newMediaIds
        )
        
        // Then
        assertTrue(result is Resource.Success)
        val updatedDiary = (result as Resource.Success).data
        assertEquals(newTitle, updatedDiary.title)
        assertEquals(newContent, updatedDiary.content)
        assertEquals(newMediaIds, updatedDiary.mediaIds)
        assertTrue(updatedDiary.updatedAt.isAfter(originalDiary.updatedAt))
        
        coVerify { diaryRepository.updateDiary(any()) }
    }
    
    @Test
    fun `invoke keeps original values when no new values provided`() = runTest {
        // Given
        val originalDiary = Diary(
            id = "diary123",
            childId = "child123",
            title = "元のタイトル",
            content = "元の内容",
            mediaIds = listOf("media1"),
            date = LocalDate.now(),
            createdAt = LocalDateTime.now().minusHours(1),
            updatedAt = LocalDateTime.now().minusHours(1)
        )
        
        coEvery { diaryRepository.updateDiary(any()) } answers { Resource.Success(firstArg()) }
        
        // When
        val result = updateDiaryUseCase.invoke(originalDiary)
        
        // Then
        assertTrue(result is Resource.Success)
        val updatedDiary = (result as Resource.Success).data
        assertEquals(originalDiary.title, updatedDiary.title)
        assertEquals(originalDiary.content, updatedDiary.content)
        assertEquals(originalDiary.mediaIds, updatedDiary.mediaIds)
        assertTrue(updatedDiary.updatedAt.isAfter(originalDiary.updatedAt))
    }
    
    @Test
    fun `invoke returns error when repository fails`() = runTest {
        // Given
        val diary = mockk<Diary>()
        val errorMessage = "Update failed"
        
        coEvery { diaryRepository.updateDiary(any()) } returns Resource.Error(errorMessage)
        
        // When
        val result = updateDiaryUseCase.invoke(diary, "新タイトル")
        
        // Then
        assertTrue(result is Resource.Error)
        assertEquals(errorMessage, (result as Resource.Error).message)
    }
}