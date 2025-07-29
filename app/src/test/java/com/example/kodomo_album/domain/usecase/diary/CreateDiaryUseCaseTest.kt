package com.example.kodomo_album.domain.usecase.diary

import com.example.kodomo_album.domain.model.Diary
import com.example.kodomo_album.domain.repository.DiaryRepository
import com.example.kodomo_album.core.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate
import java.time.LocalDateTime

class CreateDiaryUseCaseTest {
    
    private lateinit var diaryRepository: DiaryRepository
    private lateinit var createDiaryUseCase: CreateDiaryUseCase
    
    @Before
    fun setUp() {
        diaryRepository = mockk()
        createDiaryUseCase = CreateDiaryUseCase(diaryRepository)
    }
    
    @Test
    fun `invoke creates diary successfully`() = runTest {
        // Given
        val childId = "child123"
        val title = "今日の出来事"
        val content = "今日は初めて笑顔を見せてくれました"
        val mediaIds = listOf("media1", "media2")
        val date = LocalDate.now()
        
        val expectedDiary = Diary(
            id = "diary123",
            childId = childId,
            title = title,
            content = content,
            mediaIds = mediaIds,
            date = date,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        coEvery { diaryRepository.createDiary(any()) } returns Resource.Success(expectedDiary)
        
        // When
        val result = createDiaryUseCase.invoke(childId, title, content, mediaIds, date)
        
        // Then
        assertTrue(result is Resource.Success)
        val diary = (result as Resource.Success).data
        assertEquals(childId, diary.childId)
        assertEquals(title, diary.title)
        assertEquals(content, diary.content)
        assertEquals(mediaIds, diary.mediaIds)
        assertEquals(date, diary.date)
        
        coVerify { diaryRepository.createDiary(any()) }
    }
    
    @Test
    fun `invoke creates diary with default values`() = runTest {
        // Given
        val childId = "child123"
        val title = "タイトル"
        val content = "内容"
        
        coEvery { diaryRepository.createDiary(any()) } returns Resource.Success(mockk())
        
        // When
        val result = createDiaryUseCase.invoke(childId, title, content)
        
        // Then
        assertTrue(result is Resource.Success)
        coVerify { 
            diaryRepository.createDiary(match { diary ->
                diary.childId == childId &&
                diary.title == title &&
                diary.content == content &&
                diary.mediaIds.isEmpty() &&
                diary.date == LocalDate.now()
            })
        }
    }
    
    @Test
    fun `invoke returns error when repository fails`() = runTest {
        // Given
        val childId = "child123"
        val title = "タイトル"
        val content = "内容"
        val errorMessage = "Database error"
        
        coEvery { diaryRepository.createDiary(any()) } returns Resource.Error(errorMessage)
        
        // When
        val result = createDiaryUseCase.invoke(childId, title, content)
        
        // Then
        assertTrue(result is Resource.Error)
        assertEquals(errorMessage, (result as Resource.Error).message)
    }
}