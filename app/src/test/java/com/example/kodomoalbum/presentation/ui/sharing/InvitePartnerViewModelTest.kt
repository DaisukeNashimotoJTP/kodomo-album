package com.example.kodomoalbum.presentation.ui.sharing

import com.example.kodomoalbum.domain.model.Invitation
import com.example.kodomoalbum.domain.model.InvitationStatus
import com.example.kodomoalbum.domain.usecase.sharing.InvitePartnerUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class InvitePartnerViewModelTest {

    private lateinit var invitePartnerUseCase: InvitePartnerUseCase
    private lateinit var viewModel: InvitePartnerViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        invitePartnerUseCase = mockk()
        viewModel = InvitePartnerViewModel(invitePartnerUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be correct`() {
        // Given & When
        val uiState = viewModel.uiState.value

        // Then
        assertFalse(uiState.isLoading)
        assertNull(uiState.invitation)
        assertFalse(uiState.isSuccess)
        assertNull(uiState.error)
    }

    @Test
    fun `invitePartner with success should update state correctly`() = runTest {
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

        coEvery { invitePartnerUseCase(email, familyName) } returns Result.success(mockInvitation)

        // When
        viewModel.invitePartner(email, familyName)

        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertEquals(mockInvitation, uiState.invitation)
        assertTrue(uiState.isSuccess)
        assertNull(uiState.error)
        coVerify { invitePartnerUseCase(email, familyName) }
    }

    @Test
    fun `invitePartner with failure should update state correctly`() = runTest {
        // Given
        val email = "partner@example.com"
        val familyName = "田中家"
        val errorMessage = "Invalid email format"

        coEvery { invitePartnerUseCase(email, familyName) } returns Result.failure(Exception(errorMessage))

        // When
        viewModel.invitePartner(email, familyName)

        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertNull(uiState.invitation)
        assertFalse(uiState.isSuccess)
        assertEquals(errorMessage, uiState.error)
        coVerify { invitePartnerUseCase(email, familyName) }
    }

    @Test
    fun `clearState should reset state to initial`() = runTest {
        // Given
        val email = "partner@example.com"
        val familyName = "田中家"
        coEvery { invitePartnerUseCase(email, familyName) } returns Result.failure(Exception("Error"))
        
        viewModel.invitePartner(email, familyName)
        
        // When
        viewModel.clearState()

        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertNull(uiState.invitation)
        assertFalse(uiState.isSuccess)
        assertNull(uiState.error)
    }
}