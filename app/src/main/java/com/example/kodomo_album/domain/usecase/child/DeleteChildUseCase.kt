package com.example.kodomo_album.domain.usecase.child

import com.example.kodomo_album.data.repository.ChildRepository
import javax.inject.Inject

class DeleteChildUseCase @Inject constructor(
    private val childRepository: ChildRepository
) {
    suspend operator fun invoke(childId: String): Result<Unit> {
        return if (childId.isBlank()) {
            Result.failure(IllegalArgumentException("子どもIDが無効です"))
        } else {
            childRepository.deleteChild(childId)
        }
    }
}