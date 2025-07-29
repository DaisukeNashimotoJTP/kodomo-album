package com.example.kodomoalbum.domain.usecase.sharing

import com.example.kodomoalbum.domain.repository.SharingRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ShareContentUseCaseTest {

    private lateinit var sharingRepository: SharingRepository
    private lateinit var shareContentUseCase: ShareContentUseCase

    @Before
    fun setup() {
        sharingRepository = mockk()
        shareContentUseCase = ShareContentUseCase(sharingRepository)
    }

    @Test
    fun `invoke with valid parameters should return success`() = runTest {
        // Given
        val contentId = "content123"
        val contentType = "PHOTO"
        val targetUsers = listOf("user1", "user2")

        coEvery { sharingRepository.shareContent(contentId, contentType, targetUsers) } returns Result.success(Unit)

        // When
        val result = shareContentUseCase(contentId, contentType, targetUsers)

        // Then
        assertTrue(result.isSuccess)
        coVerify { sharingRepository.shareContent(contentId, contentType, targetUsers) }
    }

    @Test
    fun `invoke with blank content ID should return failure`() = runTest {
        // Given
        val contentId = ""
        val contentType = "PHOTO"
        val targetUsers = listOf("user1", "user2")

        // When
        val result = shareContentUseCase(contentId, contentType, targetUsers)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Content ID cannot be empty", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke with blank content type should return failure`() = runTest {
        // Given
        val contentId = "content123"
        val contentType = ""
        val targetUsers = listOf("user1", "user2")

        // When
        val result = shareContentUseCase(contentId, contentType, targetUsers)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Content type cannot be empty", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke with empty target users should return failure`() = runTest {
        // Given
        val contentId = "content123"
        val contentType = "PHOTO"
        val targetUsers = emptyList<String>()

        // When
        val result = shareContentUseCase(contentId, contentType, targetUsers)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Target users cannot be empty", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke with repository failure should return failure`() = runTest {
        // Given
        val contentId = "content123"
        val contentType = "PHOTO"
        val targetUsers = listOf("user1", "user2")
        val errorMessage = "Sharing failed"

        coEvery { sharingRepository.shareContent(contentId, contentType, targetUsers) } returns Result.failure(Exception(errorMessage))

        // When
        val result = shareContentUseCase(contentId, contentType, targetUsers)

        // Then
        assertTrue(result.isFailure)
        assertEquals(errorMessage, result.exceptionOrNull()?.message)
        coVerify { sharingRepository.shareContent(contentId, contentType, targetUsers) }
    }
}