package com.example.kodomo_album.domain.usecase.growth

import com.example.kodomo_album.core.util.Resource
import com.example.kodomo_album.domain.model.GrowthRecord
import com.example.kodomo_album.domain.repository.GrowthRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDate

class GetGrowthHistoryUseCaseTest {
    
    @MockK
    private lateinit var growthRepository: GrowthRepository
    
    private lateinit var getGrowthHistoryUseCase: GetGrowthHistoryUseCase
    
    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        getGrowthHistoryUseCase = GetGrowthHistoryUseCase(growthRepository)
    }
    
    @Test
    fun `invoke with valid child id should return growth history`() = runTest {
        // Given
        val childId = "child1"
        val growthRecords = listOf(
            GrowthRecord(
                id = "growth1",
                childId = childId,
                height = 70.0,
                weight = 8.0,
                headCircumference = 39.5,
                recordedAt = LocalDate.of(2024, 1, 1),
                notes = "1ヶ月健診"
            ),
            GrowthRecord(
                id = "growth2",
                childId = childId,
                height = 75.0,
                weight = 9.2,
                headCircumference = 41.0,
                recordedAt = LocalDate.of(2024, 2, 1),
                notes = "2ヶ月健診"
            )
        )
        
        coEvery { growthRepository.getGrowthHistory(childId) } returns flowOf(Resource.Success(growthRecords))
        
        // When
        val result = getGrowthHistoryUseCase(childId).first()
        
        // Then
        assertTrue(result is Resource.Success)
        assertEquals(2, result.data?.size)
        assertEquals(growthRecords, result.data)
        coVerify { growthRepository.getGrowthHistory(childId) }
    }
    
    @Test
    fun `invoke with empty child id should return error`() = runTest {
        // When
        val result = getGrowthHistoryUseCase("").first()
        
        // Then
        assertTrue(result is Resource.Error)
        assertEquals("子どもIDが指定されていません", result.message)
    }
    
    @Test
    fun `invoke with repository error should return error`() = runTest {
        // Given
        val childId = "child1"
        coEvery { growthRepository.getGrowthHistory(childId) } returns flowOf(Resource.Error("データベースエラー"))
        
        // When
        val result = getGrowthHistoryUseCase(childId).first()
        
        // Then
        assertTrue(result is Resource.Error)
        assertEquals("データベースエラー", result.message)
        coVerify { growthRepository.getGrowthHistory(childId) }
    }
    
    @Test
    fun `invoke with no data should return empty list`() = runTest {
        // Given
        val childId = "child1"
        coEvery { growthRepository.getGrowthHistory(childId) } returns flowOf(Resource.Success(emptyList()))
        
        // When
        val result = getGrowthHistoryUseCase(childId).first()
        
        // Then
        assertTrue(result is Resource.Success)
        assertTrue(result.data?.isEmpty() == true)
        coVerify { growthRepository.getGrowthHistory(childId) }
    }
}