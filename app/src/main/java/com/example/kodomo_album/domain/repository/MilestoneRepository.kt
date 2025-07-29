package com.example.kodomo_album.domain.repository

import com.example.kodomo_album.core.util.Resource
import com.example.kodomo_album.domain.model.Milestone
import kotlinx.coroutines.flow.Flow

interface MilestoneRepository {
    suspend fun createMilestone(milestone: Milestone): Resource<Milestone>
    suspend fun getMilestones(childId: String): Flow<List<Milestone>>
    suspend fun updateMilestone(milestone: Milestone): Resource<Milestone>
    suspend fun deleteMilestone(milestoneId: String): Resource<Unit>
    suspend fun getMilestoneById(milestoneId: String): Resource<Milestone>
}