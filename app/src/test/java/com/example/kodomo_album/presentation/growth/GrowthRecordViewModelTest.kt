package com.example.kodomo_album.presentation.growth

import com.example.kodomo_album.core.util.Resource
import com.example.kodomo_album.domain.model.GrowthRecord
import com.example.kodomo_album.domain.usecase.growth.RecordGrowthUseCase
import com.example.kodomo_album.domain.usecase.growth.GetGrowthHistoryUseCase
import com.example.kodomo_album.domain.usecase.growth.UpdateGrowthRecordUseCase
import com.example.kodomo_album.domain.usecase.growth.DeleteGrowthRecordUseCase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class GrowthRecordViewModelTest {
    
    @MockK
    private lateinit var recordGrowthUseCase: RecordGrowthUseCase
    
    @MockK
    private lateinit var getGrowthHistoryUseCase: GetGrowthHistoryUseCase
    
    @MockK
    private lateinit var updateGrowthRecordUseCase: UpdateGrowthRecordUseCase
    
    @MockK
    private lateinit var deleteGrowthRecordUseCase: DeleteGrowthRecordUseCase
    
    private lateinit var viewModel: GrowthRecordViewModel
    
    private val testDispatcher = UnconfinedTestDispatcher()
    
    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = GrowthRecordViewModel(
            recordGrowthUseCase,
            getGrowthHistoryUseCase,
            updateGrowthRecordUseCase,
            deleteGrowthRecordUseCase
        )
    }
    
    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `saveGrowthRecord with valid data should update state to success`() = runTest {
        // Given
        val childId = "child1"
        val height = "70.5"
        val weight = "8.2"
        val headCircumference = "40.0"
        val notes = "順調な成長"
        val recordedDate = LocalDate.of(2024, 1, 15)
        
        val expectedGrowthRecord = GrowthRecord(
            id = "",
            childId = childId,
            height = 70.5,
            weight = 8.2,
            headCircumference = 40.0,
            recordedAt = recordedDate,
            notes = notes
        )
        
        coEvery { recordGrowthUseCase(any()) } returns Resource.Success(expectedGrowthRecord)
        
        // When
        viewModel.updateHeight(height)
        viewModel.updateWeight(weight)
        viewModel.updateHeadCircumference(headCircumference)
        viewModel.updateNotes(notes)
        viewModel.updateRecordedDate(recordedDate)
        viewModel.saveGrowthRecord(childId)
        
        // Then
        assertFalse(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.error)
        assertTrue(viewModel.state.value.isSaved)
        coVerify { recordGrowthUseCase(any()) }
    }
    
    @Test
    fun `saveGrowthRecord with empty child id should show error`() = runTest {
        // When
        viewModel.saveGrowthRecord("")
        
        // Then
        assertFalse(viewModel.state.value.isLoading)
        assertEquals("子どもIDが必要です", viewModel.state.value.error)
        assertFalse(viewModel.state.value.isSaved)
    }
    
    @Test
    fun `saveGrowthRecord with no measurements should show error`() = runTest {
        // Given
        val childId = "child1"
        
        // When
        viewModel.saveGrowthRecord(childId)
        
        // Then
        assertFalse(viewModel.state.value.isLoading)
        assertEquals("身長、体重、頭囲のうち少なくとも1つは入力してください", viewModel.state.value.error)
        assertFalse(viewModel.state.value.isSaved)
    }
    
    @Test
    fun `loadGrowthHistory should update growth records list`() = runTest {
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
            )
        )
        
        coEvery { getGrowthHistoryUseCase(childId) } returns flowOf(Resource.Success(growthRecords))
        
        // When
        viewModel.loadGrowthHistory(childId)
        
        // Then
        assertEquals(growthRecords, viewModel.state.value.growthRecords)
        assertFalse(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.error)
        coVerify { getGrowthHistoryUseCase(childId) }
    }
    
    @Test
    fun `updateHeight with valid string should update state`() {
        // When
        viewModel.updateHeight("75.5")
        
        // Then
        assertEquals("75.5", viewModel.state.value.height)
    }
    
    @Test
    fun `updateWeight with valid string should update state`() {
        // When
        viewModel.updateWeight("9.2")
        
        // Then
        assertEquals("9.2", viewModel.state.value.weight)
    }
    
    @Test
    fun `updateHeadCircumference with valid string should update state`() {
        // When
        viewModel.updateHeadCircumference("41.0")
        
        // Then
        assertEquals("41.0", viewModel.state.value.headCircumference)
    }
    
    @Test
    fun `updateNotes should update state`() {
        // When
        viewModel.updateNotes("テスト記録")
        
        // Then
        assertEquals("テスト記録", viewModel.state.value.notes)
    }
    
    @Test
    fun `updateRecordedDate should update state`() {
        // Given
        val date = LocalDate.of(2024, 2, 15)
        
        // When
        viewModel.updateRecordedDate(date)
        
        // Then
        assertEquals(date, viewModel.state.value.recordedDate)
    }
    
    @Test
    fun `clearError should reset error state`() {
        // Given
        viewModel.saveGrowthRecord("") // This will set an error
        
        // When
        viewModel.clearError()
        
        // Then
        assertNull(viewModel.state.value.error)
    }
}