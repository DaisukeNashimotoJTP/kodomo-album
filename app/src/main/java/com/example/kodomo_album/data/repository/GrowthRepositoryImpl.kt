package com.example.kodomo_album.data.repository

import com.example.kodomo_album.core.util.Resource
import com.example.kodomo_album.data.firebase.datasource.GrowthRecordFirestoreDataSource
import com.example.kodomo_album.data.local.dao.GrowthRecordDao
import com.example.kodomo_album.data.local.entity.GrowthRecordEntity
import com.example.kodomo_album.data.mapper.FirebaseMapper
import com.example.kodomo_album.domain.model.*
import com.example.kodomo_album.domain.repository.GrowthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GrowthRepositoryImpl @Inject constructor(
    private val localDao: GrowthRecordDao,
    private val remoteDataSource: GrowthRecordFirestoreDataSource,
    private val firebaseMapper: FirebaseMapper
) : GrowthRepository, BaseRepository() {

    override suspend fun recordGrowth(growth: GrowthRecord): Resource<GrowthRecord> {
        return safeApiCall {
            val entity = domainToEntity(growth)
            localDao.insertGrowthRecord(entity)
            
            // リモートにも保存を試行
            try {
                val firebaseModel = firebaseMapper.domainToFirebaseGrowthRecord(growth)
                remoteDataSource.createGrowthRecord(firebaseModel)
                localDao.markAsSynced(growth.id)
            } catch (e: Exception) {
                // オフライン時は後で同期
            }
            
            growth
        }
    }

    override fun getGrowthHistory(childId: String): Flow<Resource<List<GrowthRecord>>> {
        return localDao.getGrowthRecordsByChildId(childId)
            .map { entities ->
                try {
                    val growthRecords = entities.map { entityToDomain(it) }
                    Resource.Success(growthRecords)
                } catch (e: Exception) {
                    Resource.Error("成長記録の取得に失敗しました: ${e.message}")
                }
            }
    }

    override fun getGrowthHistoryByDateRange(
        childId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<Resource<List<GrowthRecord>>> {
        val startTimestamp = startDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000
        val endTimestamp = endDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000
        
        return localDao.getGrowthRecordsByDateRange(childId, startTimestamp, endTimestamp)
            .map { entities ->
                try {
                    val growthRecords = entities.map { entityToDomain(it) }
                    Resource.Success(growthRecords)
                } catch (e: Exception) {
                    Resource.Error("期間指定成長記録の取得に失敗しました: ${e.message}")
                }
            }
    }

    override suspend fun getGrowthRecordById(recordId: String): Resource<GrowthRecord?> {
        return safeApiCall {
            val entity = localDao.getGrowthRecordById(recordId)
            entity?.let { entityToDomain(it) }
        }
    }

    override suspend fun updateGrowthRecord(growth: GrowthRecord): Resource<GrowthRecord> {
        return safeApiCall {
            val entity = domainToEntity(growth)
            localDao.updateGrowthRecord(entity)
            
            // リモートも更新を試行
            try {
                val firebaseModel = firebaseMapper.domainToFirebaseGrowthRecord(growth)
                remoteDataSource.updateGrowthRecord(firebaseModel)
                localDao.markAsSynced(growth.id)
            } catch (e: Exception) {
                // オフライン時は後で同期
            }
            
            growth
        }
    }

    override suspend fun deleteGrowthRecord(recordId: String): Resource<Unit> {
        return safeApiCall {
            localDao.deleteGrowthRecordById(recordId)
            
            // リモートからも削除を試行
            try {
                remoteDataSource.deleteGrowthRecord(recordId)
            } catch (e: Exception) {
                // オフライン時は後で同期
            }
        }
    }

    override suspend fun getGrowthChart(
        childId: String,
        type: GrowthType,
        period: GrowthPeriod
    ): Resource<GrowthChartData> {
        return safeApiCall {
            val allRecords = localDao.getGrowthRecordsByChildId(childId)
            val records = mutableListOf<GrowthRecordEntity>()
            
            // Flowから最新データを取得
            allRecords.collect { entities ->
                records.clear()
                records.addAll(entities)
            }
            
            val filteredRecords = filterByPeriod(records, period)
            val chartData = createChartData(filteredRecords, type)
            
            chartData
        }
    }

    override suspend fun getGrowthSummary(
        childId: String,
        period: GrowthPeriod
    ): Resource<GrowthSummary> {
        return safeApiCall {
            val latestRecord = localDao.getLatestGrowthRecord(childId)
            val allRecords = mutableListOf<GrowthRecordEntity>()
            
            localDao.getGrowthRecordsByChildId(childId).collect { entities ->
                allRecords.clear()
                allRecords.addAll(entities)
            }
            
            createGrowthSummary(childId, allRecords, latestRecord, period)
        }
    }

    override suspend fun getLatestGrowthRecord(childId: String): Resource<GrowthRecord?> {
        return safeApiCall {
            val entity = localDao.getLatestGrowthRecord(childId)
            entity?.let { entityToDomain(it) }
        }
    }

    private fun domainToEntity(growth: GrowthRecord): GrowthRecordEntity {
        return GrowthRecordEntity(
            id = growth.id,
            childId = growth.childId,
            height = growth.height,
            weight = growth.weight,
            headCircumference = growth.headCircumference,
            recordedAt = growth.recordedAt.atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000,
            notes = growth.notes,
            createdAt = System.currentTimeMillis(),
            isSynced = false
        )
    }

    private fun entityToDomain(entity: GrowthRecordEntity): GrowthRecord {
        return GrowthRecord(
            id = entity.id,
            childId = entity.childId,
            height = entity.height,
            weight = entity.weight,
            headCircumference = entity.headCircumference,
            recordedAt = LocalDate.ofEpochDay(entity.recordedAt / (1000 * 60 * 60 * 24)),
            notes = entity.notes
        )
    }

    private fun filterByPeriod(records: List<GrowthRecordEntity>, period: GrowthPeriod): List<GrowthRecordEntity> {
        if (period == GrowthPeriod.ALL) return records
        
        val now = System.currentTimeMillis()
        val cutoffTime = when (period) {
            GrowthPeriod.WEEK -> now - (7 * 24 * 60 * 60 * 1000L)
            GrowthPeriod.MONTH -> now - (30 * 24 * 60 * 60 * 1000L)
            GrowthPeriod.THREE_MONTHS -> now - (90 * 24 * 60 * 60 * 1000L)
            GrowthPeriod.SIX_MONTHS -> now - (180 * 24 * 60 * 60 * 1000L)
            GrowthPeriod.YEAR -> now - (365 * 24 * 60 * 60 * 1000L)
            GrowthPeriod.ALL -> 0L
        }
        
        return records.filter { it.recordedAt >= cutoffTime }
    }

    private fun createChartData(records: List<GrowthRecordEntity>, type: GrowthType): GrowthChartData {
        val heightData = mutableListOf<GrowthDataPoint>()
        val weightData = mutableListOf<GrowthDataPoint>()
        val headCircumferenceData = mutableListOf<GrowthDataPoint>()
        
        records.forEach { record ->
            val date = LocalDate.ofEpochDay(record.recordedAt / (1000 * 60 * 60 * 24))
            
            record.height?.let { height ->
                if (type == GrowthType.HEIGHT || type == GrowthType.ALL) {
                    heightData.add(GrowthDataPoint(date, height))
                }
            }
            
            record.weight?.let { weight ->
                if (type == GrowthType.WEIGHT || type == GrowthType.ALL) {
                    weightData.add(GrowthDataPoint(date, weight))
                }
            }
            
            record.headCircumference?.let { headCircumference ->
                if (type == GrowthType.HEAD_CIRCUMFERENCE || type == GrowthType.ALL) {
                    headCircumferenceData.add(GrowthDataPoint(date, headCircumference))
                }
            }
        }
        
        return GrowthChartData(
            heightData = heightData,
            weightData = weightData,
            headCircumferenceData = headCircumferenceData
        )
    }

    private fun createGrowthSummary(
        childId: String,
        records: List<GrowthRecordEntity>,
        latestRecord: GrowthRecordEntity?,
        period: GrowthPeriod
    ): GrowthSummary {
        val periodName = when (period) {
            GrowthPeriod.WEEK -> "1週間"
            GrowthPeriod.MONTH -> "1ヶ月"
            GrowthPeriod.THREE_MONTHS -> "3ヶ月"
            GrowthPeriod.SIX_MONTHS -> "6ヶ月"
            GrowthPeriod.YEAR -> "1年"
            GrowthPeriod.ALL -> "全期間"
        }
        
        // 成長の変化を計算
        val sortedRecords = records.sortedBy { it.recordedAt }
        val firstRecord = sortedRecords.firstOrNull()
        
        val heightGrowth = if (firstRecord != null && latestRecord != null) {
            (latestRecord.height ?: 0.0) - (firstRecord.height ?: 0.0)
        } else null
        
        val weightGrowth = if (firstRecord != null && latestRecord != null) {
            (latestRecord.weight ?: 0.0) - (firstRecord.weight ?: 0.0)
        } else null
        
        return GrowthSummary(
            childId = childId,
            period = periodName,
            latestHeight = latestRecord?.height,
            latestWeight = latestRecord?.weight,
            latestHeadCircumference = latestRecord?.headCircumference,
            heightGrowth = heightGrowth,
            weightGrowth = weightGrowth,
            totalRecords = records.size,
            recordPeriod = latestRecord?.let { 
                LocalDate.ofEpochDay(it.recordedAt / (1000 * 60 * 60 * 24))
            } ?: LocalDate.now()
        )
    }
}