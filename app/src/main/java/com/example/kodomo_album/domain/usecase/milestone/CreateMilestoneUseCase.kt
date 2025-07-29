package com.example.kodomo_album.domain.usecase.milestone

import com.example.kodomo_album.core.util.Resource
import com.example.kodomo_album.domain.model.Milestone
import com.example.kodomo_album.domain.repository.MilestoneRepository
import javax.inject.Inject

class CreateMilestoneUseCase @Inject constructor(
    private val repository: MilestoneRepository
) {
    suspend operator fun invoke(milestone: Milestone): Resource<Milestone> {
        return repository.createMilestone(milestone)
    }
}