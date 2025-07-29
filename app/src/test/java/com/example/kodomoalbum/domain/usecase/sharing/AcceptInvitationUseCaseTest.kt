package com.example.kodomoalbum.domain.usecase.sharing

import com.example.kodomoalbum.domain.repository.SharingRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AcceptInvitationUseCaseTest {

    private lateinit var sharingRepository: SharingRepository
    private lateinit var acceptInvitationUseCase: AcceptInvitationUseCase

    @Before
    fun setup() {
        sharingRepository = mockk()
        acceptInvitationUseCase = AcceptInvitationUseCase(sharingRepository)
    }

    @Test
    fun `invoke with valid invitation ID should return success`() = runTest {
        // Given
        val invitationId = "invitation123"

        coEvery { sharingRepository.acceptInvitation(invitationId) } returns Result.success(Unit)

        // When
        val result = acceptInvitationUseCase(invitationId)

        // Then
        assertTrue(result.isSuccess)
        coVerify { sharingRepository.acceptInvitation(invitationId) }
    }

    @Test
    fun `invoke with blank invitation ID should return failure`() = runTest {
        // Given
        val invitationId = ""

        // When
        val result = acceptInvitationUseCase(invitationId)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Invitation ID cannot be empty", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke with repository failure should return failure`() = runTest {
        // Given
        val invitationId = "invitation123"
        val errorMessage = "Invitation not found"

        coEvery { sharingRepository.acceptInvitation(invitationId) } returns Result.failure(Exception(errorMessage))

        // When
        val result = acceptInvitationUseCase(invitationId)

        // Then
        assertTrue(result.isFailure)
        assertEquals(errorMessage, result.exceptionOrNull()?.message)
        coVerify { sharingRepository.acceptInvitation(invitationId) }
    }
}