package com.example.kodomoalbum.data.repository

import com.example.kodomo_album.data.local.dao.*
import com.example.kodomoalbum.data.model.*
import com.example.kodomo_album.domain.model.Child
import com.example.kodomo_album.domain.model.Gender
import com.example.kodomoalbum.domain.repository.DashboardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepositoryImpl @Inject constructor(
    private val childDao: ChildDao,
    private val mediaDao: MediaDao,
    private val diaryDao: DiaryDao,
    private val eventDao: EventDao,
    private val milestoneDao: MilestoneDao,
    private val growthRecordDao: GrowthRecordDao
) : DashboardRepository {
    
    override suspend fun getDashboardData(childId: String): Flow<DashboardData> = flow {
        val child = childDao.getChildById(childId)?.let { entity ->
            Child(
                id = entity.id,
                userId = entity.userId,
                name = entity.name,
                birthDate = entity.birthDate,
                gender = Gender.valueOf(entity.gender),
                profileImageUrl = entity.profileImageUrl,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt
            )
        } ?: return@flow
        
        // 最近のアクティビティを取得
        val recentActivities = getRecentActivities(childId)
        
        // 成長サマリーを取得
        val growthSummary = getGrowthSummary(child)
        
        // 統計情報を取得
        val statistics = getStatistics(childId)
        
        // 今後のイベントを取得
        val upcomingEvents = getUpcomingEvents(childId)
        
        emit(DashboardData(
            child = child,
            recentActivities = recentActivities,
            growthSummary = growthSummary,
            statistics = statistics,
            upcomingEvents = upcomingEvents
        ))
    }
    
    override suspend fun refreshDashboardData(childId: String) {
        // データの同期処理などがあればここに実装
    }
    
    private suspend fun getRecentActivities(childId: String): List<RecentActivity> {
        val activities = mutableListOf<RecentActivity>()
        
        // 最近の写真
        val recentMedia = mediaDao.getMediaByChildId(childId).toString() // 仮実装
        
        // 最近の日記
        val recentDiaries = diaryDao.getDiariesByChildId(childId).take(3)
        recentDiaries.forEach { diary ->
            activities.add(RecentActivity(
                id = diary.id,
                type = ActivityType.DIARY_CREATED,
                title = diary.title,
                description = diary.content,
                date = diary.date.atStartOfDay()
            ))
        }
        
        // 最近のマイルストーン
        val recentMilestones = milestoneDao.getMilestonesByChildId(childId).toString() // 仮実装
        
        // 最近のイベント
        val recentEvents = eventDao.getEventsByChildId(childId).toString() // 仮実装
        
        return activities.sortedByDescending { it.date }.take(10)
    }
    
    private suspend fun getGrowthSummary(child: Child): GrowthSummary {
        val growthRecords = growthRecordDao.getGrowthRecordsByChildId(child.id)
        val latestRecord = growthRecords.maxByOrNull { it.recordedAt }
        val previousRecord = growthRecords
            .filter { it.recordedAt < (latestRecord?.recordedAt ?: LocalDate.now()) }
            .maxByOrNull { it.recordedAt }
        
        val currentAge = Period.between(child.birthDate, LocalDate.now()).toTotalMonths().toInt()
        
        return GrowthSummary(
            currentAge = currentAge,
            latestHeight = latestRecord?.height,
            latestWeight = latestRecord?.weight,
            heightGrowth = if (latestRecord?.height != null && previousRecord?.height != null) {
                latestRecord.height - previousRecord.height
            } else null,
            weightGrowth = if (latestRecord?.weight != null && previousRecord?.weight != null) {
                latestRecord.weight - previousRecord.weight
            } else null,
            lastMeasuredDate = latestRecord?.recordedAt
        )
    }
    
    private suspend fun getStatistics(childId: String): Statistics {
        val currentMonth = LocalDate.now().withDayOfMonth(1)
        val nextMonth = currentMonth.plusMonths(1)
        
        // 仮実装 - 実際のクエリに置き換える必要があります
        return Statistics(
            totalPhotos = 0, // mediaDao.countMediaByChildId(childId)
            totalDiaries = diaryDao.getDiariesByChildId(childId).size,
            totalEvents = 0, // eventDao.countEventsByChildId(childId)
            totalMilestones = 0, // milestoneDao.countMilestonesByChildId(childId)
            thisMonthPhotos = 0, // mediaDao.countMediaByChildIdAndMonth(childId, currentMonth)
            thisMonthDiaries = 0 // diaryDao.countDiariesByChildIdAndMonth(childId, currentMonth)
        )
    }
    
    private suspend fun getUpcomingEvents(childId: String): List<Event> {
        val today = LocalDate.now()
        val nextMonth = today.plusMonths(1)
        
        // 今後1ヶ月のイベントを取得（仮実装）
        return emptyList() // eventDao.getEventsByDateRange(childId, today, nextMonth)
    }
}