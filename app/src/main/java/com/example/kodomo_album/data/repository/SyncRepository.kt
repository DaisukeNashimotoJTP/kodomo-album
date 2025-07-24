package com.example.kodomo_album.data.repository

import com.example.kodomo_album.data.firebase.*
import com.example.kodomo_album.data.local.dao.ChildDao
import com.example.kodomo_album.data.local.dao.DiaryDao
import com.example.kodomo_album.data.local.dao.MediaDao
import com.example.kodomo_album.data.local.entity.ChildEntity
import com.example.kodomo_album.data.local.entity.DiaryEntity
import com.example.kodomo_album.data.local.entity.MediaEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepository @Inject constructor(
    private val firestoreService: FirestoreService,
    private val syncService: SyncService,
    private val offlineManager: OfflineManager,
    private val childDao: ChildDao,
    private val diaryDao: DiaryDao,
    private val mediaDao: MediaDao
) {

    // ネットワーク状態の監視
    val networkState: StateFlow<NetworkState> = offlineManager.networkState
    val syncQueue: StateFlow<List<SyncItem>> = offlineManager.syncQueue

    // 子ども情報の操作
    suspend fun createChild(child: ChildEntity): Result<ChildEntity> {
        return try {
            // ローカルに保存
            childDao.insertChild(child)
            
            // オンラインの場合は即座に同期、オフラインの場合はキューに追加
            if (offlineManager.isNetworkAvailable()) {
                val syncResult = firestoreService.syncChild(child)
                if (syncResult.isFailure) {
                    offlineManager.queueForSync(SyncItem(child.id, SyncItemType.CHILD))
                }
            } else {
                offlineManager.queueForSync(SyncItem(child.id, SyncItemType.CHILD))
            }
            
            Result.success(child)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateChild(child: ChildEntity): Result<ChildEntity> {
        return try {
            childDao.update(child)
            
            if (offlineManager.isNetworkAvailable()) {
                val syncResult = firestoreService.syncChild(child)
                if (syncResult.isFailure) {
                    offlineManager.queueForSync(SyncItem(child.id, SyncItemType.CHILD))
                }
            } else {
                offlineManager.queueForSync(SyncItem(child.id, SyncItemType.CHILD))
            }
            
            Result.success(child)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteChild(childId: String): Result<Unit> {
        return try {
            if (offlineManager.isNetworkAvailable()) {
                // オンラインの場合は同期削除
                firestoreService.deleteChildWithSync(childId)
            } else {
                // オフラインの場合はローカル削除のみ
                childDao.deleteById(childId)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getChildrenFlow(userId: String): Flow<List<ChildEntity>> {
        return if (offlineManager.isNetworkAvailable()) {
            firestoreService.getChildrenFlow(userId)
        } else {
            childDao.getChildrenByUserIdFlow(userId)
        }
    }

    // 日記の操作
    suspend fun createDiary(diary: DiaryEntity): Result<DiaryEntity> {
        return try {
            val diaryToSave = diary.copy(isSynced = offlineManager.isNetworkAvailable())
            diaryDao.insertDiary(diaryToSave)
            
            if (offlineManager.isNetworkAvailable()) {
                val syncResult = firestoreService.syncDiary(diaryToSave)
                if (syncResult.isFailure) {
                    offlineManager.queueForSync(SyncItem(diary.id, SyncItemType.DIARY))
                }
            } else {
                offlineManager.queueForSync(SyncItem(diary.id, SyncItemType.DIARY))
            }
            
            Result.success(diaryToSave)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateDiary(diary: DiaryEntity): Result<DiaryEntity> {
        return try {
            val diaryToUpdate = diary.copy(
                isSynced = false,
                updatedAt = System.currentTimeMillis()
            )
            diaryDao.update(diaryToUpdate)
            
            if (offlineManager.isNetworkAvailable()) {
                val syncResult = firestoreService.syncDiary(diaryToUpdate)
                if (syncResult.isFailure) {
                    offlineManager.queueForSync(SyncItem(diary.id, SyncItemType.DIARY))
                }
            } else {
                offlineManager.queueForSync(SyncItem(diary.id, SyncItemType.DIARY))
            }
            
            Result.success(diaryToUpdate)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteDiary(diaryId: String): Result<Unit> {
        return try {
            if (offlineManager.isNetworkAvailable()) {
                firestoreService.deleteDiaryWithSync(diaryId)
            } else {
                diaryDao.deleteById(diaryId)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getDiariesFlow(childId: String): Flow<List<DiaryEntity>> {
        return if (offlineManager.isNetworkAvailable()) {
            firestoreService.getDiariesFlow(childId)
        } else {
            diaryDao.getDiariesByChildIdFlow(childId)
        }
    }

    // メディアの操作
    suspend fun createMedia(media: MediaEntity): Result<MediaEntity> {
        return try {
            val mediaToSave = media.copy(isUploaded = offlineManager.isNetworkAvailable())
            mediaDao.insertMedia(mediaToSave)
            
            if (offlineManager.isNetworkAvailable()) {
                val syncResult = firestoreService.syncMedia(mediaToSave)
                if (syncResult.isFailure) {
                    offlineManager.queueForSync(SyncItem(media.id, SyncItemType.MEDIA))
                }
            } else {
                offlineManager.queueForSync(SyncItem(media.id, SyncItemType.MEDIA))
            }
            
            Result.success(mediaToSave)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateMedia(media: MediaEntity): Result<MediaEntity> {
        return try {
            val mediaToUpdate = media.copy(
                isUploaded = false,
                uploadedAt = System.currentTimeMillis()
            )
            mediaDao.update(mediaToUpdate)
            
            if (offlineManager.isNetworkAvailable()) {
                val syncResult = firestoreService.syncMedia(mediaToUpdate)
                if (syncResult.isFailure) {
                    offlineManager.queueForSync(SyncItem(media.id, SyncItemType.MEDIA))
                }
            } else {
                offlineManager.queueForSync(SyncItem(media.id, SyncItemType.MEDIA))
            }
            
            Result.success(mediaToUpdate)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteMedia(mediaId: String): Result<Unit> {
        return try {
            if (offlineManager.isNetworkAvailable()) {
                firestoreService.deleteMediaWithSync(mediaId)
            } else {
                mediaDao.deleteById(mediaId)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getMediaFlow(childId: String): Flow<List<MediaEntity>> {
        return if (offlineManager.isNetworkAvailable()) {
            firestoreService.getMediaFlow(childId)
        } else {
            mediaDao.getMediaByChildId(childId)
        }
    }

    // 同期管理
    fun startAutoSync(userId: String) {
        syncService.startAutoSync(userId)
    }

    fun stopAutoSync() {
        syncService.stopAutoSync()
    }

    suspend fun performManualSync(userId: String): Result<Unit> {
        return syncService.performManualSync(userId)
    }

    suspend fun getSyncStatus(userId: String): SyncStatus {
        return syncService.getSyncStatus(userId)
    }

    fun setAutoSyncEnabled(enabled: Boolean, userId: String? = null) {
        syncService.setAutoSyncEnabled(enabled, userId)
    }

    // オフライン管理
    fun isNetworkAvailable(): Boolean = offlineManager.isNetworkAvailable()
    
    fun canWorkOffline(): Boolean = offlineManager.canWorkOffline()
    
    fun getQueueStats(): QueueStats = offlineManager.getQueueStats()
    
    suspend fun processQueueManually(): Result<Unit> = offlineManager.processQueueManually()

    // クリーンアップ
    fun cleanup() {
        syncService.cleanup()
        offlineManager.cleanup()
    }
}