package com.example.kodomo_album.domain.usecase.media

import android.content.Context
import com.example.kodomo_album.core.util.Resource
import com.example.kodomo_album.domain.model.Media
import com.example.kodomo_album.domain.model.MediaMetadata
import com.example.kodomo_album.domain.model.MediaType
import com.example.kodomo_album.domain.repository.MediaRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.io.File
import java.time.LocalDateTime

class UploadMediaUseCaseTest {

    private lateinit var mediaRepository: MediaRepository
    private lateinit var context: Context
    private lateinit var uploadMediaUseCase: UploadMediaUseCase

    @Before
    fun setUp() {
        mediaRepository = mockk()
        context = mockk(relaxed = true)
        uploadMediaUseCase = UploadMediaUseCase(mediaRepository, context)
    }

    @Test
    fun `uploadPhoto should return success when repository succeeds`() = runTest {
        // Given
        val file = mockk<File>()
        val metadata = MediaMetadata(
            childId = "child1",
            caption = "Test photo",
            type = MediaType.PHOTO
        )
        val expectedMedia = Media(
            id = "media1",
            childId = "child1",
            type = MediaType.PHOTO,
            url = "https://example.com/photo.jpg",
            thumbnailUrl = "https://example.com/thumb.jpg",
            caption = "Test photo",
            takenAt = LocalDateTime.now(),
            uploadedAt = LocalDateTime.now()
        )

        coEvery { mediaRepository.uploadPhoto(any(), any()) } returns Resource.Success(expectedMedia)

        // When
        val result = uploadMediaUseCase(file, metadata)

        // Then
        assertTrue(result is Resource.Success)
        assertEquals(expectedMedia, (result as Resource.Success).data)
        coVerify { mediaRepository.uploadPhoto(any(), metadata) }
    }

    @Test
    fun `uploadVideo should return success when repository succeeds`() = runTest {
        // Given
        val file = mockk<File>()
        val metadata = MediaMetadata(
            childId = "child1",
            caption = "Test video",
            type = MediaType.VIDEO
        )
        val expectedMedia = Media(
            id = "media1",
            childId = "child1",
            type = MediaType.VIDEO,
            url = "https://example.com/video.mp4",
            thumbnailUrl = null,
            caption = "Test video",
            takenAt = LocalDateTime.now(),
            uploadedAt = LocalDateTime.now()
        )

        coEvery { mediaRepository.uploadVideo(any(), any()) } returns Resource.Success(expectedMedia)

        // When
        val result = uploadMediaUseCase(file, metadata)

        // Then
        assertTrue(result is Resource.Success)
        assertEquals(expectedMedia, (result as Resource.Success).data)
        coVerify { mediaRepository.uploadVideo(any(), metadata) }
    }

    @Test
    fun `uploadPhoto should return error when repository fails`() = runTest {
        // Given
        val file = mockk<File>()
        val metadata = MediaMetadata(
            childId = "child1",
            type = MediaType.PHOTO
        )
        val errorMessage = "Upload failed"

        coEvery { mediaRepository.uploadPhoto(any(), any()) } returns Resource.Error(errorMessage)

        // When
        val result = uploadMediaUseCase(file, metadata)

        // Then
        assertTrue(result is Resource.Error)
        assertEquals(errorMessage, (result as Resource.Error).message)
    }
}