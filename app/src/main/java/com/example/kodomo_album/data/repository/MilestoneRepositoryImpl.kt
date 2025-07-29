package com.example.kodomo_album.data.repository

import com.example.kodomo_album.core.util.Resource
import com.example.kodomo_album.data.local.dao.MilestoneDao
import com.example.kodomo_album.data.local.entity.MilestoneEntity
import com.example.kodomo_album.domain.model.Milestone
import com.example.kodomo_album.domain.model.MilestoneType
import com.example.kodomo_album.domain.repository.MilestoneRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MilestoneRepositoryImpl @Inject constructor(
    private val milestoneDao: MilestoneDao,
    private val gson: Gson
) : BaseRepository(), MilestoneRepository {

    override suspend fun createMilestone(milestone: Milestone): Resource<Milestone> {
        return safeApiCall {
            val entity = milestoneToEntity(milestone)
            milestoneDao.insertMilestone(entity)
            milestone
        }
    }

    override suspend fun getMilestones(childId: String): Flow<List<Milestone>> {
        return milestoneDao.getMilestonesByChildId(childId).map { entities ->
            entities.map { entityToMilestone(it) }
        }
    }

    override suspend fun updateMilestone(milestone: Milestone): Resource<Milestone> {
        return safeApiCall {
            val entity = milestoneToEntity(milestone)
            milestoneDao.updateMilestone(entity)
            milestone
        }
    }

    override suspend fun deleteMilestone(milestoneId: String): Resource<Unit> {
        return safeApiCall {
            milestoneDao.deleteMilestoneById(milestoneId)
        }
    }

    override suspend fun getMilestoneById(milestoneId: String): Resource<Milestone> {
        return safeApiCall {
            val entity = milestoneDao.getMilestoneById(milestoneId)
                ?: throw Exception("マイルストーンが見つかりません")
            entityToMilestone(entity)
        }
    }

    private fun milestoneToEntity(milestone: Milestone): MilestoneEntity {
        return MilestoneEntity(
            id = milestone.id,
            childId = milestone.childId,
            type = milestone.type.name,
            title = milestone.title,
            description = milestone.description,
            achievedAt = milestone.achievedAt.toEpochDay() * 24 * 60 * 60 * 1000, // 日付をUnix timestampに変換
            mediaIds = gson.toJson(milestone.mediaIds),
            createdAt = milestone.createdAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            isSynced = false
        )
    }

    private fun entityToMilestone(entity: MilestoneEntity): Milestone {
        val mediaIds = try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson<List<String>>(entity.mediaIds, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        return Milestone(
            id = entity.id,
            childId = entity.childId,
            type = MilestoneType.valueOf(entity.type),
            title = entity.title,
            description = entity.description,
            achievedAt = LocalDate.ofEpochDay(entity.achievedAt / (24 * 60 * 60 * 1000)),
            mediaIds = mediaIds,
            createdAt = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(entity.createdAt),
                ZoneId.systemDefault()
            )
        )
    }
}