package com.example.kodomo_album.domain.usecase.child

import com.example.kodomo_album.data.repository.ChildRepository
import com.example.kodomo_album.domain.model.Child
import javax.inject.Inject

class GetChildByIdUseCase @Inject constructor(
    private val childRepository: ChildRepository
) {
    suspend operator fun invoke(childId: String): Child? {
        return if (childId.isBlank()) {
            null
        } else {
            childRepository.getChildById(childId)
        }
    }
}