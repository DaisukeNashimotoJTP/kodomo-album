package com.example.kodomo_album.data.repository

import com.example.kodomo_album.data.firebase.datasource.ChildFirestoreDataSource
import com.example.kodomo_album.data.local.dao.ChildDao
import com.example.kodomo_album.data.mapper.FirebaseMapper
import com.example.kodomo_album.domain.model.Child
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChildRepositoryImpl @Inject constructor(
    private val childDao: ChildDao,
    private val childFirestoreDataSource: ChildFirestoreDataSource,
    private val mapper: FirebaseMapper
) : ChildRepository, BaseRepository() {

    override fun getChildrenByUserIdFlow(userId: String): Flow<List<Child>> {
        return childDao.getChildrenByUserIdFlow(userId)
            .map { entities -> entities.map { mapper.childEntityToDomain(it) } }
    }

    override suspend fun getChildrenByUserId(userId: String): List<Child> {
        return safeCall {
            val entities = childDao.getChildrenByUserId(userId)
            entities.map { mapper.childEntityToDomain(it) }
        } ?: emptyList()
    }

    override suspend fun getChildById(childId: String): Child? {
        return safeCall {
            val entity = childDao.getChildById(childId)
            entity?.let { mapper.childEntityToDomain(it) }
        }
    }

    override suspend fun saveChild(child: Child): Result<Child> {
        return try {
            val childWithId = if (child.id.isEmpty()) {
                child.copy(id = UUID.randomUUID().toString())
            } else {
                child
            }

            val entity = mapper.domainToChildEntity(childWithId)
            
            // Save to local database
            childDao.insertChild(entity)
            
            // Sync to Firebase
            val firebaseChild = mapper.childEntityToFirebase(entity)
            childFirestoreDataSource.createChild(firebaseChild)
                .onFailure { 
                    // If Firebase sync fails, still return success for offline capability
                    // Background sync will handle retry
                }
            
            Result.success(childWithId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateChild(child: Child): Result<Unit> {
        return try {
            val updatedChild = child.copy(updatedAt = System.currentTimeMillis())
            val entity = mapper.domainToChildEntity(updatedChild)
            
            // Update local database
            childDao.update(entity)
            
            // Sync to Firebase
            val firebaseChild = mapper.childEntityToFirebase(entity)
            childFirestoreDataSource.updateChild(firebaseChild)
                .onFailure { 
                    // Log error but don't fail the operation for offline capability
                }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteChild(childId: String): Result<Unit> {
        return try {
            // Delete from local database
            childDao.deleteById(childId)
            
            // Delete from Firebase
            childFirestoreDataSource.deleteChild(childId)
                .onFailure { 
                    // Log error but don't fail the operation for offline capability
                }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}