package com.example.kodomo_album.domain.usecase.child

import com.example.kodomo_album.data.repository.ChildRepository
import com.example.kodomo_album.domain.model.Child
import com.example.kodomo_album.domain.model.Gender
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class CreateChildUseCaseTest {

    @MockK
    private lateinit var childRepository: ChildRepository

    private lateinit var createChildUseCase: CreateChildUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        createChildUseCase = CreateChildUseCase(childRepository)
    }

    @Test
    fun `invoke with valid child data should return success`() = runTest {
        // Given
        val mockChild = Child(
            id = "child123",
            name = "Test Child",
            birthDate = LocalDate.of(2023, 1, 1),
            gender = Gender.MALE,
            profileImageUrl = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        coEvery { childRepository.saveChild(any()) } returns Result.success(mockChild)

        // When
        val result = createChildUseCase(mockChild)

        // Then
        assert(result.isSuccess)
        val returnedChild = result.getOrNull()!!
        assert(returnedChild.id == mockChild.id)
        assert(returnedChild.name == mockChild.name)
        assert(returnedChild.birthDate == mockChild.birthDate)
        assert(returnedChild.gender == mockChild.gender)
        coVerify { childRepository.saveChild(mockChild) }
    }

    @Test
    fun `invoke with repository error should return error`() = runTest {
        // Given
        val mockChild = Child(
            id = "child123",
            name = "Test Child",
            birthDate = LocalDate.of(2023, 1, 1),
            gender = Gender.FEMALE,
            profileImageUrl = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val exception = Exception("Failed to create child")

        coEvery { childRepository.saveChild(any()) } returns Result.failure(exception)

        // When
        val result = createChildUseCase(mockChild)

        // Then
        assert(result.isFailure)
        val errorResult = result.exceptionOrNull()!!
        assert(errorResult.message == "Failed to create child")
        coVerify { childRepository.saveChild(mockChild) }
    }

    @Test
    fun `invoke should validate child data before creating`() = runTest {
        // Given
        val validChild = Child(
            id = "child123",
            name = "Valid Child",
            birthDate = LocalDate.of(2023, 1, 1),
            gender = Gender.MALE,
            profileImageUrl = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        coEvery { childRepository.saveChild(any()) } returns Result.success(validChild)

        // When
        val result = createChildUseCase(validChild)

        // Then
        assert(result.isSuccess)
        coVerify { childRepository.saveChild(validChild) }
    }

    @Test
    fun `invoke with empty name should return error`() = runTest {
        // Given
        val invalidChild = Child(
            id = "child123",
            name = "", // Empty name
            birthDate = LocalDate.of(2023, 1, 1),
            gender = Gender.MALE,
            profileImageUrl = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        // When
        val result = createChildUseCase(invalidChild)

        // Then
        assert(result.isFailure)
        val errorMessage = result.exceptionOrNull()?.message
        assert(errorMessage?.contains("名前") ?: false)
        coVerify { childRepository wasNot Called }
    }

    @Test
    fun `invoke should handle repository exception`() = runTest {
        // Given
        val mockChild = Child(
            id = "child123",
            name = "Test Child",
            birthDate = LocalDate.of(2023, 1, 1),
            gender = Gender.MALE,
            profileImageUrl = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        coEvery { childRepository.saveChild(any()) } throws Exception("Database error")

        // When
        val result = createChildUseCase(mockChild)

        // Then
        assert(result.isFailure)
        val errorMessage = result.exceptionOrNull()?.message
        assert(errorMessage?.contains("Database error") ?: false)
        coVerify { childRepository.saveChild(mockChild) }
    }
}