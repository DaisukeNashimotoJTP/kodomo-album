package com.example.kodomo_album.domain.repository

import com.example.kodomo_album.core.util.Resource
import com.example.kodomo_album.domain.model.GrowthRecord
import com.example.kodomo_album.domain.model.GrowthChartData
import com.example.kodomo_album.domain.model.GrowthSummary
import com.example.kodomo_album.domain.model.GrowthType
import com.example.kodomo_album.domain.model.GrowthPeriod
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface GrowthRepository {
    suspend fun recordGrowth(growth: GrowthRecord): Resource<GrowthRecord>
    
    fun getGrowthHistory(childId: String): Flow<Resource<List<GrowthRecord>>>
    
    fun getGrowthHistoryByDateRange(
        childId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<Resource<List<GrowthRecord>>>
    
    suspend fun getGrowthRecordById(recordId: String): Resource<GrowthRecord?>
    
    suspend fun updateGrowthRecord(growth: GrowthRecord): Resource<GrowthRecord>
    
    suspend fun deleteGrowthRecord(recordId: String): Resource<Unit>
    
    suspend fun getGrowthChart(
        childId: String,
        type: GrowthType,
        period: GrowthPeriod = GrowthPeriod.ALL
    ): Resource<GrowthChartData>
    
    suspend fun getGrowthSummary(
        childId: String,
        period: GrowthPeriod = GrowthPeriod.MONTH
    ): Resource<GrowthSummary>
    
    suspend fun getLatestGrowthRecord(childId: String): Resource<GrowthRecord?>
}