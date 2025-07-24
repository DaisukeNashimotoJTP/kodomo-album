package com.example.kodomo_album.data.firebase

import android.util.Log
import com.example.kodomo_album.data.local.dao.ChildDao
import com.example.kodomo_album.data.local.dao.DiaryDao
import com.example.kodomo_album.data.local.dao.MediaDao
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncService @Inject constructor(
    private val firestoreService: FirestoreService,
    private val childDao: ChildDao,
    private val diaryDao: DiaryDao,
    private val mediaDao: MediaDao
) {
    companion object {
        private const val TAG = "SyncService"
        private const val SYNC_INTERVAL_MS = 30_000L // 30秒間隔
    }

    private val syncScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var syncJob: Job? = null
    private var isAutoSyncEnabled = true

    // 自動同期の開始
    fun startAutoSync(userId: String) {
        stopAutoSync()
        
        syncJob = syncScope.launch {
            while (isActive && isAutoSyncEnabled) {
                try {
                    performFullSync(userId)
                    delay(SYNC_INTERVAL_MS)
                } catch (e: Exception) {
                    Log.e(TAG, "Auto sync failed", e)
                    delay(SYNC_INTERVAL_MS)
                }
            }
        }
        Log.d(TAG, "Auto sync started for user: $userId")
    }

    // 自動同期の停止
    fun stopAutoSync() {
        syncJob?.cancel()
        syncJob = null
        Log.d(TAG, "Auto sync stopped")
    }

    // 手動同期
    suspend fun performManualSync(userId: String): Result<Unit> {
        return try {
            performFullSync(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Manual sync failed", e)
            Result.failure(e)
        }
    }

    // フル同期の実行
    private suspend fun performFullSync(userId: String) {
        Log.d(TAG, "Starting full sync for user: $userId")

        // 1. ローカルの未同期データをFirestoreへアップロード
        uploadUnsyncedData()

        // 2. Firestoreからローカルへダウンロード
        firestoreService.syncFromFirestore(userId)

        Log.d(TAG, "Full sync completed for user: $userId")
    }

    // 未同期データのアップロード
    private suspend fun uploadUnsyncedData() {
        try {
            // 未同期の日記をアップロード
            val unsyncedDiaries = diaryDao.getUnsyncedDiaries()
            unsyncedDiaries.forEach { diary ->
                val result = firestoreService.syncDiary(diary)
                if (result.isFailure) {
                    Log.w(TAG, "Failed to sync diary: ${diary.id}", result.exceptionOrNull())
                }
            }

            // 未アップロードのメディアをアップロード  
            val unuploadedMedia = mediaDao.getUnuploadedMedia()
            unuploadedMedia.forEach { media ->
                val result = firestoreService.syncMedia(media)
                if (result.isFailure) {
                    Log.w(TAG, "Failed to sync media: ${media.id}", result.exceptionOrNull())
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload unsynced data", e)
            throw e
        }
    }

    // 同期状態の確認
    suspend fun getSyncStatus(userId: String): SyncStatus {
        return try {
            val unsyncedDiaries = diaryDao.getUnsyncedDiaries().size
            val unuploadedMedia = mediaDao.getUnuploadedMedia().size
            
            SyncStatus(
                isAutoSyncEnabled = isAutoSyncEnabled,
                pendingDiaries = unsyncedDiaries,
                pendingMedia = unuploadedMedia,
                lastSyncTime = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get sync status", e)
            SyncStatus(
                isAutoSyncEnabled = isAutoSyncEnabled,
                pendingDiaries = -1,
                pendingMedia = -1,
                lastSyncTime = -1
            )
        }
    }

    // リアルタイム同期の有効/無効切り替え
    fun setAutoSyncEnabled(enabled: Boolean, userId: String? = null) {
        isAutoSyncEnabled = enabled
        if (enabled && userId != null) {
            startAutoSync(userId)
        } else {
            stopAutoSync()
        }
    }

    // 特定データの即座同期
    suspend fun syncSpecificDiary(diaryId: String): Result<Unit> {
        return try {
            val diary = diaryDao.getDiaryById(diaryId)
            if (diary != null && !diary.isSynced) {
                firestoreService.syncDiary(diary)
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync specific diary: $diaryId", e)
            Result.failure(e)
        }
    }

    suspend fun syncSpecificMedia(mediaId: String): Result<Unit> {
        return try {
            val media = mediaDao.getMediaById(mediaId)
            if (media != null && !media.isUploaded) {
                firestoreService.syncMedia(media)
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync specific media: $mediaId", e)
            Result.failure(e)
        }
    }

    // リソースクリーンアップ
    fun cleanup() {
        stopAutoSync()
        syncScope.cancel()
    }
}

data class SyncStatus(
    val isAutoSyncEnabled: Boolean,
    val pendingDiaries: Int,
    val pendingMedia: Int,
    val lastSyncTime: Long
)