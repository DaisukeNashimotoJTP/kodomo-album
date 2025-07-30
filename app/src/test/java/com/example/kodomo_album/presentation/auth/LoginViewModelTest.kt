package com.example.kodomo_album.presentation.auth

import com.example.kodomo_album.domain.usecase.auth.SignInUseCase
import com.example.kodomo_album.core.util.Resource
import com.example.kodomo_album.domain.model.User
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @MockK
    private lateinit var signInUseCase: SignInUseCase

    private lateinit var viewModel: LoginViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = LoginViewModel(signInUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onEvent EmailChanged should update email state`() {
        // Given
        val email = "test@example.com"

        // When
        viewModel.onEvent(LoginEvent.EmailChanged(email))

        // Then
        assert(viewModel.state.email == email)
    }

    @Test
    fun `onEvent PasswordChanged should update password state`() {
        // Given
        val password = "password123"

        // When
        viewModel.onEvent(LoginEvent.PasswordChanged(password))

        // Then
        assert(viewModel.state.password == password)
    }

    @Test
    fun `onEvent Login with valid credentials should trigger login`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val mockUser = User(
            id = "user123",
            email = email,
            displayName = "Test User",
            profileImageUrl = "",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        coEvery { signInUseCase(email, password) } returns Resource.Success(mockUser)

        // When
        viewModel.onEvent(LoginEvent.EmailChanged(email))
        viewModel.onEvent(LoginEvent.PasswordChanged(password))
        viewModel.onEvent(LoginEvent.Login)
        advanceUntilIdle()

        // Then
        val state = viewModel.state
        assert(!state.isLoading)
        coVerify { signInUseCase(email, password) }
    }

    @Test
    fun `onEvent Login should set loading state during operation`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"

        coEvery { signInUseCase(email, password) } returns Resource.Success(
            User(
                id = "user123",
                email = email,
                displayName = "Test User",
                profileImageUrl = "",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )

        // When
        viewModel.onEvent(LoginEvent.EmailChanged(email))
        viewModel.onEvent(LoginEvent.PasswordChanged(password))
        viewModel.onEvent(LoginEvent.Login)

        // Then (during loading)
        assert(viewModel.state.isLoading)

        advanceUntilIdle()

        // Then (after loading)
        assert(!viewModel.state.isLoading)
    }
}