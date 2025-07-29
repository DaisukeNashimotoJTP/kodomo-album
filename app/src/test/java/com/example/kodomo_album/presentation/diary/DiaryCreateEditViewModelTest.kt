package com.example.kodomo_album.presentation.diary

import com.example.kodomo_album.domain.model.Diary
import com.example.kodomo_album.domain.usecase.diary.CreateDiaryUseCase
import com.example.kodomo_album.domain.usecase.diary.UpdateDiaryUseCase
import com.example.kodomo_album.domain.usecase.diary.DeleteDiaryUseCase
import com.example.kodomo_album.domain.repository.DiaryRepository
import com.example.kodomo_album.core.util.Resource
import com.example.kodomo_album.core.util.UiEvent
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDate
import java.time.LocalDateTime

@ExperimentalCoroutinesApi
class DiaryCreateEditViewModelTest {

    private lateinit var createDiaryUseCase: CreateDiaryUseCase
    private lateinit var updateDiaryUseCase: UpdateDiaryUseCase
    private lateinit var deleteDiaryUseCase: DeleteDiaryUseCase
    private lateinit var diaryRepository: DiaryRepository
    private lateinit var viewModel: DiaryCreateEditViewModel
    
    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        
        createDiaryUseCase = mockk()
        updateDiaryUseCase = mockk()
        deleteDiaryUseCase = mockk()
        diaryRepository = mockk()
        
        viewModel = DiaryCreateEditViewModel(
            createDiaryUseCase,
            updateDiaryUseCase,
            deleteDiaryUseCase,
            diaryRepository
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `updateTitle updates ui state title`() {
        // Given
        val newTitle = "新しいタイトル"
        
        // When
        viewModel.updateTitle(newTitle)
        
        // Then
        assertEquals(newTitle, viewModel.uiState.value.title)
    }

    @Test
    fun `updateContent updates ui state content`() {
        // Given
        val newContent = "新しい内容"
        
        // When
        viewModel.updateContent(newContent)
        
        // Then
        assertEquals(newContent, viewModel.uiState.value.content)
    }

    @Test
    fun `addMedia adds media id to selected list`() {
        // Given
        val mediaId = "media123"
        
        // When
        viewModel.addMedia(mediaId)
        
        // Then
        assertTrue(viewModel.uiState.value.selectedMediaIds.contains(mediaId))
    }

    @Test
    fun `addMedia does not add duplicate media id`() {
        // Given
        val mediaId = "media123"
        viewModel.addMedia(mediaId)
        
        // When
        viewModel.addMedia(mediaId)
        
        // Then
        assertEquals(1, viewModel.uiState.value.selectedMediaIds.size)
    }

    @Test
    fun `removeMedia removes media id from selected list`() {
        // Given
        val mediaId = "media123"
        viewModel.addMedia(mediaId)
        
        // When
        viewModel.removeMedia(mediaId)
        
        // Then
        assertFalse(viewModel.uiState.value.selectedMediaIds.contains(mediaId))
    }

    @Test
    fun `saveDiary shows error when title is blank`() = runTest {
        // Given
        val childId = "child123"
        viewModel.updateTitle("")
        viewModel.updateContent("内容")
        
        // When
        viewModel.saveDiary(childId)
        
        // Then
        var eventReceived: UiEvent? = null
        val job = launch(UnconfinedTestDispatcher()) {
            viewModel.uiEvent.collect { event ->
                eventReceived = event
            }
        }
        
        advanceUntilIdle()
        
        assertTrue(eventReceived is UiEvent.ShowSnackbar)
        assertEquals("タイトルを入力してください", (eventReceived as UiEvent.ShowSnackbar).message)
        
        job.cancel()
    }

    @Test
    fun `saveDiary shows error when content is blank`() = runTest {
        // Given
        val childId = "child123"
        viewModel.updateTitle("タイトル")
        viewModel.updateContent("")
        
        // When
        viewModel.saveDiary(childId)
        
        // Then
        var eventReceived: UiEvent? = null
        val job = launch(UnconfinedTestDispatcher()) {
            viewModel.uiEvent.collect { event ->
                eventReceived = event
            }
        }
        
        advanceUntilIdle()
        
        assertTrue(eventReceived is UiEvent.ShowSnackbar)
        assertEquals("内容を入力してください", (eventReceived as UiEvent.ShowSnackbar).message)
        
        job.cancel()
    }

    @Test
    fun `saveDiary creates new diary successfully`() = runTest {
        // Given
        val childId = "child123"
        val title = "タイトル"
        val content = "内容"
        val mockDiary = mockk<Diary>()
        
        viewModel.updateTitle(title)
        viewModel.updateContent(content)
        
        coEvery { 
            createDiaryUseCase(any(), any(), any(), any(), any()) 
        } returns Resource.Success(mockDiary)
        
        // When
        viewModel.saveDiary(childId)
        advanceUntilIdle()
        
        // Then
        coVerify { createDiaryUseCase(childId, title, content, emptyList(), any()) }
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loadDiary loads diary data successfully`() = runTest {
        // Given
        val diaryId = "diary123"
        val mockDiary = Diary(
            id = diaryId,
            childId = "child123",
            title = "テストタイトル",
            content = "テスト内容",
            mediaIds = listOf("media1", "media2"),
            date = LocalDate.now(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        coEvery { diaryRepository.getDiaryById(diaryId) } returns Resource.Success(mockDiary)
        
        // When
        viewModel.loadDiary(diaryId)
        advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assertEquals(mockDiary.title, uiState.title)
        assertEquals(mockDiary.content, uiState.content)
        assertEquals(mockDiary.mediaIds, uiState.selectedMediaIds)
        assertTrue(uiState.isEditMode)
        assertEquals(mockDiary, uiState.currentDiary)
        assertFalse(uiState.isLoading)
    }

    @Test
    fun `deleteDiary deletes diary successfully`() = runTest {
        // Given
        val mockDiary = mockk<Diary> {
            every { id } returns "diary123"
        }
        
        // Set up edit mode
        viewModel.updateTitle("タイトル")
        viewModel.updateContent("内容")
        // Simulate loaded diary
        coEvery { diaryRepository.getDiaryById("diary123") } returns Resource.Success(mockDiary)
        viewModel.loadDiary("diary123")
        advanceUntilIdle()
        
        coEvery { deleteDiaryUseCase("diary123") } returns Resource.Success(Unit)
        
        // When
        viewModel.deleteDiary()
        advanceUntilIdle()
        
        // Then
        coVerify { deleteDiaryUseCase("diary123") }
        assertFalse(viewModel.uiState.value.isLoading)
    }
}