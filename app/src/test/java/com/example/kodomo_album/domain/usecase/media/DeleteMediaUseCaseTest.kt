package com.example.kodomo_album.domain.usecase.media

import com.example.kodomo_album.core.util.Resource
import com.example.kodomo_album.domain.repository.MediaRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DeleteMediaUseCaseTest {

    @MockK
    private lateinit var mediaRepository: MediaRepository

    private lateinit var deleteMediaUseCase: DeleteMediaUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        deleteMediaUseCase = DeleteMediaUseCase(mediaRepository)
    }

    @Test
    fun `invoke should return success when repository delete succeeds`() = runTest {
        // Given
        val mediaId = "media123"
        coEvery { mediaRepository.deleteMedia(mediaId) } returns Resource.Success(Unit)

        // When
        val result = deleteMediaUseCase(mediaId)

        // Then
        assert(result is Resource.Success)
        coVerify { mediaRepository.deleteMedia(mediaId) }
    }

    @Test
    fun `invoke should return error when repository delete fails`() = runTest {
        // Given
        val mediaId = "media123"
        val errorMessage = "Delete failed"
        coEvery { mediaRepository.deleteMedia(mediaId) } returns Resource.Error(errorMessage)

        // When
        val result = deleteMediaUseCase(mediaId)

        // Then
        assert(result is Resource.Error)
        assert((result as Resource.Error).message == errorMessage)
        coVerify { mediaRepository.deleteMedia(mediaId) }
    }

    @Test
    fun `invoke should return error when repository throws exception`() = runTest {
        // Given
        val mediaId = "media123"
        val exception = Exception("Network error")
        coEvery { mediaRepository.deleteMedia(mediaId) } throws exception

        // When
        val result = deleteMediaUseCase(mediaId)

        // Then
        assert(result is Resource.Error)
        assert((result as Resource.Error).message == "Network error")
        coVerify { mediaRepository.deleteMedia(mediaId) }
    }

    @Test
    fun `invoke should return generic error message when exception message is null`() = runTest {
        // Given
        val mediaId = "media123"
        val exception = Exception()
        coEvery { mediaRepository.deleteMedia(mediaId) } throws exception

        // When
        val result = deleteMediaUseCase(mediaId)

        // Then
        assert(result is Resource.Error)
        assert((result as Resource.Error).message == "メディアの削除に失敗しました")
        coVerify { mediaRepository.deleteMedia(mediaId) }
    }
}