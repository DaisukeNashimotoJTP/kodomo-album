package com.example.kodomoalbum.domain.usecase

import com.example.kodomoalbum.data.model.DashboardData
import com.example.kodomoalbum.domain.repository.DashboardRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardUseCase @Inject constructor(
    private val dashboardRepository: DashboardRepository
) {
    
    suspend fun getDashboardData(childId: String): Flow<DashboardData> {
        return dashboardRepository.getDashboardData(childId)
    }
    
    suspend fun refreshDashboardData(childId: String) {
        dashboardRepository.refreshDashboardData(childId)
    }
}