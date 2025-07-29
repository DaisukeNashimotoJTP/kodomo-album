package com.example.kodomoalbum.domain.usecase.sharing

import com.example.kodomoalbum.domain.model.Invitation
import com.example.kodomoalbum.domain.model.InvitationStatus
import com.example.kodomoalbum.domain.repository.SharingRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class InvitePartnerUseCaseTest {

    private lateinit var sharingRepository: SharingRepository
    private lateinit var invitePartnerUseCase: InvitePartnerUseCase

    @Before
    fun setup() {
        sharingRepository = mockk()
        invitePartnerUseCase = InvitePartnerUseCase(sharingRepository)
    }

    @Test
    fun `invoke with valid parameters should return success`() = runTest {
        // Given
        val email = "partner@example.com"
        val familyName = "田中家"
        val mockInvitation = Invitation(
            id = "invitation123",
            familyId = "family123",
            inviterUserId = "user123",
            inviterName = "田中太郎",
            inviteeEmail = email,
            status = InvitationStatus.PENDING,
            createdAt = LocalDateTime.now(),
            expiresAt = LocalDateTime.now().plusDays(7)
        )

        coEvery { sharingRepository.invitePartner(email, familyName) } returns Result.success(mockInvitation)

        // When
        val result = invitePartnerUseCase(email, familyName)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(mockInvitation, result.getOrNull())
        coVerify { sharingRepository.invitePartner(email, familyName) }
    }

    @Test
    fun `invoke with blank email should return failure`() = runTest {
        // Given
        val email = ""
        val familyName = "田中家"

        // When
        val result = invitePartnerUseCase(email, familyName)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Email cannot be empty", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke with invalid email format should return failure`() = runTest {
        // Given
        val email = "invalid-email"
        val familyName = "田中家"

        // When
        val result = invitePartnerUseCase(email, familyName)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Invalid email format", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke with blank family name should return failure`() = runTest {
        // Given
        val email = "partner@example.com"
        val familyName = ""

        // When
        val result = invitePartnerUseCase(email, familyName)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Family name cannot be empty", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke with repository failure should return failure`() = runTest {
        // Given
        val email = "partner@example.com"
        val familyName = "田中家"
        val errorMessage = "Network error"

        coEvery { sharingRepository.invitePartner(email, familyName) } returns Result.failure(Exception(errorMessage))

        // When
        val result = invitePartnerUseCase(email, familyName)

        // Then
        assertTrue(result.isFailure)
        assertEquals(errorMessage, result.exceptionOrNull()?.message)
        coVerify { sharingRepository.invitePartner(email, familyName) }
    }
}