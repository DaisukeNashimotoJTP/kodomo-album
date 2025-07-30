package com.example.kodomoalbum.domain.repository

import com.example.kodomoalbum.data.model.DashboardData
import kotlinx.coroutines.flow.Flow

interface DashboardRepository {
    
    suspend fun getDashboardData(childId: String): Flow<DashboardData>
    
    suspend fun refreshDashboardData(childId: String)
}