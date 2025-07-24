package com.example.kodomo_album.domain.usecase.child

import com.example.kodomo_album.data.repository.ChildRepository
import com.example.kodomo_album.domain.model.Child
import javax.inject.Inject

class UpdateChildUseCase @Inject constructor(
    private val childRepository: ChildRepository
) {
    suspend operator fun invoke(child: Child): Result<Unit> {
        return if (child.name.isBlank()) {
            Result.failure(IllegalArgumentException("お子さまの名前を入力してください"))
        } else {
            childRepository.updateChild(child)
        }
    }
}