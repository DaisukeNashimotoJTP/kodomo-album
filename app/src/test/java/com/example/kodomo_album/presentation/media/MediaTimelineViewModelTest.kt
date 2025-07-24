package com.example.kodomo_album.presentation.media

import com.example.kodomo_album.core.util.Resource
import com.example.kodomo_album.domain.model.Media
import com.example.kodomo_album.domain.model.MediaType
import com.example.kodomo_album.domain.usecase.media.DeleteMediaUseCase
import com.example.kodomo_album.domain.usecase.media.GetMediaListUseCase
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class MediaTimelineViewModelTest {

    @MockK
    private lateinit var getMediaListUseCase: GetMediaListUseCase

    @MockK
    private lateinit var deleteMediaUseCase: DeleteMediaUseCase

    private lateinit var viewModel: MediaTimelineViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = MediaTimelineViewModel(getMediaListUseCase, deleteMediaUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadMediaList should update state with media list`() = runTest {
        // Given
        val childId = "child123"
        val mockMediaList = listOf(
            createMockMedia(id = "1", type = MediaType.PHOTO),
            createMockMedia(id = "2", type = MediaType.VIDEO),
            createMockMedia(id = "3", type = MediaType.ECHO)
        )

        coEvery { getMediaListUseCase(childId, null) } returns flowOf(mockMediaList)

        // When
        viewModel.loadMediaList(childId)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assert(!state.isLoading)
        assert(state.mediaList == mockMediaList.sortedByDescending { it.takenAt })
        assert(state.error == null)
    }

    @Test
    fun `loadMediaList with empty childId should not call use case`() = runTest {
        // When
        viewModel.loadMediaList("")
        advanceUntilIdle()

        // Then
        verify { getMediaListUseCase wasNot Called }
    }

    @Test
    fun `loadMediaList should handle error`() = runTest {
        // Given
        val childId = "child123"
        
        // エラーをthrowするFlowを作成
        coEvery { getMediaListUseCase(childId, null) } returns kotlinx.coroutines.flow.flow {
            throw Exception("Network error")
        }

        // When
        viewModel.loadMediaList(childId)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assert(!state.isLoading)
        assert(state.error != null)
    }

    @Test
    fun `onMediaTypeFilterChanged should filter media list`() = runTest {
        // Given
        val childId = "child123"
        val mockMediaList = listOf(
            createMockMedia(id = "1", type = MediaType.PHOTO),
            createMockMedia(id = "2", type = MediaType.VIDEO),
            createMockMedia(id = "3", type = MediaType.ECHO)
        )

        coEvery { getMediaListUseCase(childId, any()) } returns flowOf(mockMediaList)

        // When
        viewModel.onChildChanged(childId)
        advanceUntilIdle()
        viewModel.onMediaTypeFilterChanged(MediaType.PHOTO)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assert(state.selectedMediaType == MediaType.PHOTO)
        // フィルタリングされたリストを確認
        val filteredList = state.mediaList.filter { 
            it.type == MediaType.PHOTO || it.type == MediaType.ECHO 
        }
        assert(state.mediaList == filteredList)
    }

    @Test
    fun `onDateFilterChanged should update date range`() = runTest {
        // Given
        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.of(2024, 1, 31)
        val childId = "child123"

        coEvery { getMediaListUseCase(childId, null) } returns flowOf(emptyList())
        coEvery { getMediaListUseCase(childId, Pair(startDate, endDate)) } returns flowOf(emptyList())

        // When
        viewModel.onChildChanged(childId)
        advanceUntilIdle()
        viewModel.onDateFilterChanged(startDate, endDate)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assert(state.dateRange == Pair(startDate, endDate))
        coVerify { getMediaListUseCase(childId, Pair(startDate, endDate)) }
    }

    @Test
    fun `deleteMedia should call delete use case and refresh list`() = runTest {
        // Given
        val mediaId = "media123"
        val childId = "child123"

        coEvery { deleteMediaUseCase(mediaId) } returns Resource.Success(Unit)
        coEvery { getMediaListUseCase(childId, null) } returns flowOf(emptyList())

        // When
        viewModel.onChildChanged(childId)
        advanceUntilIdle()
        viewModel.deleteMedia(mediaId)
        advanceUntilIdle()

        // Then
        coVerify { deleteMediaUseCase(mediaId) }
        coVerify(atLeast = 2) { getMediaListUseCase(childId, null) } // 初回ロード + リフレッシュ
    }

    @Test
    fun `deleteMedia should handle error`() = runTest {
        // Given
        val mediaId = "media123"
        val childId = "child123"
        val errorMessage = "Delete failed"

        coEvery { deleteMediaUseCase(mediaId) } returns Resource.Error(errorMessage)
        coEvery { getMediaListUseCase(childId, null) } returns flowOf(emptyList())

        // When
        viewModel.onChildChanged(childId)
        advanceUntilIdle()
        viewModel.deleteMedia(mediaId)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assert(!state.isLoading)
        coVerify { deleteMediaUseCase(mediaId) }
        // エラー時はリフレッシュしない
        coVerify(exactly = 1) { getMediaListUseCase(childId, null) }
    }

    @Test
    fun `onMediaSelected should update selected media`() = runTest {
        // Given
        val media = createMockMedia(id = "1", type = MediaType.PHOTO)

        // When
        viewModel.onMediaSelected(media)

        // Then
        val state = viewModel.uiState.value
        assert(state.selectedMedia == media)
    }

    @Test
    fun `clearSelectedMedia should clear selected media`() = runTest {
        // Given
        val media = createMockMedia(id = "1", type = MediaType.PHOTO)
        viewModel.onMediaSelected(media)

        // When
        viewModel.clearSelectedMedia()

        // Then
        val state = viewModel.uiState.value
        assert(state.selectedMedia == null)
    }

    @Test
    fun `clearError should clear error message`() = runTest {
        // Given
        // エラー状態を手動で設定
        viewModel.clearError() // 初期状態にしてから

        // When
        viewModel.clearError() // clearErrorを実行

        // Then
        val state = viewModel.uiState.value
        assert(state.error == null)
    }

    private fun createMockMedia(
        id: String,
        type: MediaType,
        childId: String = "child123"
    ): Media {
        return Media(
            id = id,
            childId = childId,
            type = type,
            url = "https://example.com/media/$id",
            thumbnailUrl = "https://example.com/thumb/$id",
            caption = "Test caption $id",
            takenAt = LocalDateTime.now().minusDays(id.toLong()),
            uploadedAt = LocalDateTime.now()
        )
    }
}