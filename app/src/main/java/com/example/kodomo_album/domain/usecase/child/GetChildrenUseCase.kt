package com.example.kodomo_album.domain.usecase.child

import com.example.kodomo_album.data.repository.ChildRepository
import com.example.kodomo_album.domain.model.Child
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetChildrenUseCase @Inject constructor(
    private val childRepository: ChildRepository
) {
    fun getChildrenFlow(userId: String): Flow<List<Child>> {
        return childRepository.getChildrenByUserIdFlow(userId)
    }

    suspend fun getChildren(userId: String): List<Child> {
        return childRepository.getChildrenByUserId(userId)
    }
}