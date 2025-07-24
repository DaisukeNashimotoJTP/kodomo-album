package com.example.kodomo_album.data.repository

import com.example.kodomo_album.domain.model.Child
import kotlinx.coroutines.flow.Flow

interface ChildRepository {
    fun getChildrenByUserIdFlow(userId: String): Flow<List<Child>>
    suspend fun getChildrenByUserId(userId: String): List<Child>
    suspend fun getChildById(childId: String): Child?
    suspend fun saveChild(child: Child): Result<Child>
    suspend fun updateChild(child: Child): Result<Unit>
    suspend fun deleteChild(childId: String): Result<Unit>
}