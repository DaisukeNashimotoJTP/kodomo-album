package com.example.kodomo_album.domain.usecase.milestone

import com.example.kodomo_album.domain.model.Milestone
import com.example.kodomo_album.domain.repository.MilestoneRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMilestonesUseCase @Inject constructor(
    private val repository: MilestoneRepository
) {
    suspend operator fun invoke(childId: String): Flow<List<Milestone>> {
        return repository.getMilestones(childId)
    }
}