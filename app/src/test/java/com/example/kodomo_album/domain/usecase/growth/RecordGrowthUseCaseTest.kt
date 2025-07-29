package com.example.kodomo_album.domain.usecase.growth

import com.example.kodomo_album.core.util.Resource
import com.example.kodomo_album.domain.model.GrowthRecord
import com.example.kodomo_album.domain.repository.GrowthRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDate

class RecordGrowthUseCaseTest {
    
    @MockK
    private lateinit var growthRepository: GrowthRepository
    
    private lateinit var recordGrowthUseCase: RecordGrowthUseCase
    
    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        recordGrowthUseCase = RecordGrowthUseCase(growthRepository)
    }
    
    @Test
    fun `invoke with valid growth data should return success`() = runTest {
        // Given
        val growthRecord = GrowthRecord(
            id = "growth1",
            childId = "child1",
            height = 70.5,
            weight = 8.2,
            headCircumference = 40.0,
            recordedAt = LocalDate.of(2024, 1, 15),
            notes = "順調な成長"
        )
        
        coEvery { growthRepository.recordGrowth(any()) } returns Resource.Success(growthRecord)
        
        // When
        val result = recordGrowthUseCase(growthRecord)
        
        // Then
        assertTrue(result is Resource.Success)
        assertEquals(growthRecord, result.data)
        coVerify { growthRepository.recordGrowth(growthRecord) }
    }
    
    @Test
    fun `invoke with negative height should return error`() = runTest {
        // Given
        val invalidGrowthRecord = GrowthRecord(
            id = "growth1",
            childId = "child1",
            height = -10.0,
            weight = 8.2,
            headCircumference = 40.0,
            recordedAt = LocalDate.of(2024, 1, 15),
            notes = null
        )
        
        // When
        val result = recordGrowthUseCase(invalidGrowthRecord)
        
        // Then
        assertTrue(result is Resource.Error)
        assertEquals("身長は0以上である必要があります", result.message)
    }
    
    @Test
    fun `invoke with negative weight should return error`() = runTest {
        // Given
        val invalidGrowthRecord = GrowthRecord(
            id = "growth1",
            childId = "child1",
            height = 70.5,
            weight = -5.0,
            headCircumference = 40.0,
            recordedAt = LocalDate.of(2024, 1, 15),
            notes = null
        )
        
        // When
        val result = recordGrowthUseCase(invalidGrowthRecord)
        
        // Then
        assertTrue(result is Resource.Error)
        assertEquals("体重は0以上である必要があります", result.message)
    }
    
    @Test
    fun `invoke with negative head circumference should return error`() = runTest {
        // Given
        val invalidGrowthRecord = GrowthRecord(
            id = "growth1",
            childId = "child1",
            height = 70.5,
            weight = 8.2,
            headCircumference = -20.0,
            recordedAt = LocalDate.of(2024, 1, 15),
            notes = null
        )
        
        // When
        val result = recordGrowthUseCase(invalidGrowthRecord)
        
        // Then
        assertTrue(result is Resource.Error)
        assertEquals("頭囲は0以上である必要があります", result.message)
    }
    
    @Test
    fun `invoke with future date should return error`() = runTest {
        // Given
        val futureGrowthRecord = GrowthRecord(
            id = "growth1",
            childId = "child1",
            height = 70.5,
            weight = 8.2,
            headCircumference = 40.0,
            recordedAt = LocalDate.now().plusDays(1),
            notes = null
        )
        
        // When
        val result = recordGrowthUseCase(futureGrowthRecord)
        
        // Then
        assertTrue(result is Resource.Error)
        assertEquals("記録日は現在日付以前である必要があります", result.message)
    }
    
    @Test
    fun `invoke with all null measurements should return error`() = runTest {
        // Given
        val invalidGrowthRecord = GrowthRecord(
            id = "growth1",
            childId = "child1",
            height = null,
            weight = null,
            headCircumference = null,
            recordedAt = LocalDate.of(2024, 1, 15),
            notes = null
        )
        
        // When
        val result = recordGrowthUseCase(invalidGrowthRecord)
        
        // Then
        assertTrue(result is Resource.Error)
        assertEquals("身長、体重、頭囲のうち少なくとも1つは入力してください", result.message)
    }
    
    @Test
    fun `invoke with repository error should return error`() = runTest {
        // Given
        val growthRecord = GrowthRecord(
            id = "growth1",
            childId = "child1",
            height = 70.5,
            weight = 8.2,
            headCircumference = 40.0,
            recordedAt = LocalDate.of(2024, 1, 15),
            notes = "順調な成長"
        )
        
        coEvery { growthRepository.recordGrowth(any()) } returns Resource.Error("データベースエラー")
        
        // When
        val result = recordGrowthUseCase(growthRecord)
        
        // Then
        assertTrue(result is Resource.Error)
        assertEquals("データベースエラー", result.message)
        coVerify { growthRepository.recordGrowth(growthRecord) }
    }
}