package com.example.kodomo_album.data.firebase

import com.example.kodomo_album.data.firebase.datasource.ChildFirestoreDataSource
import com.example.kodomo_album.data.firebase.datasource.DiaryFirestoreDataSource
import com.example.kodomo_album.data.firebase.datasource.MediaFirestoreDataSource
import com.example.kodomo_album.data.firebase.models.FirebaseChild
import com.example.kodomo_album.data.firebase.models.FirebaseDiary
import com.example.kodomo_album.data.firebase.models.FirebaseMedia
import com.example.kodomo_album.data.local.dao.ChildDao
import com.example.kodomo_album.data.local.dao.DiaryDao
import com.example.kodomo_album.data.local.dao.MediaDao
import com.example.kodomo_album.data.local.entity.ChildEntity
import com.example.kodomo_album.data.local.entity.DiaryEntity
import com.example.kodomo_album.data.local.entity.MediaEntity
import com.example.kodomo_album.data.mapper.FirebaseMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreService @Inject constructor(
    private val childFirestoreDataSource: ChildFirestoreDataSource,
    private val diaryFirestoreDataSource: DiaryFirestoreDataSource,
    private val mediaFirestoreDataSource: MediaFirestoreDataSource,
    private val childDao: ChildDao,
    private val diaryDao: DiaryDao,
    private val mediaDao: MediaDao,
    private val firebaseMapper: FirebaseMapper
) {

    // 子ども情報の同期
    suspend fun syncChild(childEntity: ChildEntity): Result<Unit> {
        return try {
            val firebaseChild = firebaseMapper.childEntityToFirebase(childEntity)
            val result = childFirestoreDataSource.createChild(firebaseChild)
            if (result.isSuccess) {
                // ローカルの同期状態を更新
                val updatedEntity = childEntity.copy(updatedAt = System.currentTimeMillis())
                childDao.update(updatedEntity)
                Result.success(Unit)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Sync failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncDiary(diaryEntity: DiaryEntity): Result<Unit> {
        return try {
            val firebaseDiary = firebaseMapper.diaryEntityToFirebase(diaryEntity)
            val result = diaryFirestoreDataSource.createDiary(firebaseDiary)
            if (result.isSuccess) {
                // ローカルの同期状態を更新
                val updatedEntity = diaryEntity.copy(
                    isSynced = true,
                    updatedAt = System.currentTimeMillis()
                )
                diaryDao.update(updatedEntity)
                Result.success(Unit)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Sync failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncMedia(mediaEntity: MediaEntity): Result<Unit> {
        return try {
            val firebaseMedia = firebaseMapper.mediaEntityToFirebase(mediaEntity)
            val result = mediaFirestoreDataSource.createMedia(firebaseMedia)
            if (result.isSuccess) {
                // ローカルの同期状態を更新
                val updatedEntity = mediaEntity.copy(
                    isUploaded = true,
                    uploadedAt = System.currentTimeMillis()
                )
                mediaDao.update(updatedEntity)
                Result.success(Unit)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Sync failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 全体同期（未同期データのアップロード）
    suspend fun syncAllUnsyncedData(userId: String): Result<Unit> {
        return try {
            // 未同期の日記を同期
            val unsyncedDiaries = diaryDao.getUnsyncedDiaries()
            unsyncedDiaries.forEach { diary ->
                syncDiary(diary)
            }

            // 未アップロードのメディアを同期
            val unuploadedMedia = mediaDao.getUnuploadedMedia()
            unuploadedMedia.forEach { media ->
                syncMedia(media)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Firestoreからローカルに同期
    suspend fun syncFromFirestore(userId: String): Result<Unit> {
        return try {
            // 子ども情報を同期
            val childrenResult = childFirestoreDataSource.getChildrenByUserId(userId)
            if (childrenResult.isSuccess) {
                val firebaseChildren = childrenResult.getOrNull() ?: emptyList()
                firebaseChildren.forEach { firebaseChild ->
                    val localChild = firebaseMapper.firebaseToChildEntity(firebaseChild)
                    childDao.insertOrUpdate(localChild)
                }
            }

            // 日記を同期（子どもごと）
            val localChildren = childDao.getChildrenByUserId(userId)
            localChildren.forEach { child ->
                val diariesResult = diaryFirestoreDataSource.getDiariesByChildId(child.id)
                if (diariesResult.isSuccess) {
                    val firebaseDiaries = diariesResult.getOrNull() ?: emptyList()
                    firebaseDiaries.forEach { firebaseDiary ->
                        val localDiary = firebaseMapper.firebaseToDiaryEntity(firebaseDiary)
                        diaryDao.insertOrUpdate(localDiary.copy(isSynced = true))
                    }
                }

                // メディアを同期
                val mediaResult = mediaFirestoreDataSource.getMediaByChildId(child.id)
                if (mediaResult.isSuccess) {
                    val firebaseMedia = mediaResult.getOrNull() ?: emptyList()
                    firebaseMedia.forEach { media ->
                        val localMedia = firebaseMapper.firebaseToMediaEntity(media)
                        mediaDao.insertOrUpdate(localMedia.copy(isUploaded = true))
                    }
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // リアルタイム同期のFlowを提供
    fun getChildrenFlow(userId: String): Flow<List<ChildEntity>> {
        val firestoreFlow = childFirestoreDataSource.getChildrenByUserIdFlow(userId)
        val localFlow = childDao.getChildrenByUserIdFlow(userId)
        
        return combine(firestoreFlow, localFlow) { firebaseChildren, localChildren ->
            // Firestoreのデータをローカルに反映
            firebaseChildren.forEach { firebaseChild ->
                val localChild = firebaseMapper.firebaseToChildEntity(firebaseChild)
                childDao.insertOrUpdate(localChild)
            }
            // 最新のローカルデータを返す
            childDao.getChildrenByUserId(userId)
        }
    }

    fun getDiariesFlow(childId: String): Flow<List<DiaryEntity>> {
        val firestoreFlow = diaryFirestoreDataSource.getDiariesByChildIdFlow(childId)
        val localFlow = diaryDao.getDiariesByChildIdFlow(childId)
        
        return combine(firestoreFlow, localFlow) { firebaseDiaries, localDiaries ->
            // Firestoreのデータをローカルに反映
            firebaseDiaries.forEach { firebaseDiary ->
                val localDiary = firebaseMapper.firebaseToDiaryEntity(firebaseDiary)
                diaryDao.insertOrUpdate(localDiary.copy(isSynced = true))
            }
            // 最新のローカルデータを返す
            diaryDao.getDiariesByChildId(childId)
        }
    }

    fun getMediaFlow(childId: String): Flow<List<MediaEntity>> {
        val firestoreFlow = mediaFirestoreDataSource.getMediaByChildIdFlow(childId)
        val localFlow = mediaDao.getMediaByChildIdFlow(childId)
        
        return combine(firestoreFlow, localFlow) { firebaseMedia, localMedia ->
            // Firestoreのデータをローカルに反映
            firebaseMedia.forEach { media ->
                val localMediaEntity = firebaseMapper.firebaseToMediaEntity(media)
                mediaDao.insertOrUpdate(localMediaEntity.copy(isUploaded = true))
            }
            // 最新のローカルデータを返す
            mediaDao.getMediaByChildId(childId)
        }
    }

    // 削除時の同期
    suspend fun deleteChildWithSync(childId: String): Result<Unit> {
        return try {
            // Firestoreから削除
            val firestoreResult = childFirestoreDataSource.deleteChild(childId)
            if (firestoreResult.isSuccess) {
                // ローカルからも削除（CASCADE設定により関連データも削除）
                childDao.deleteById(childId)
                Result.success(Unit)
            } else {
                Result.failure(firestoreResult.exceptionOrNull() ?: Exception("Delete failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteDiaryWithSync(diaryId: String): Result<Unit> {
        return try {
            val firestoreResult = diaryFirestoreDataSource.deleteDiary(diaryId)
            if (firestoreResult.isSuccess) {
                diaryDao.deleteById(diaryId)
                Result.success(Unit)
            } else {
                Result.failure(firestoreResult.exceptionOrNull() ?: Exception("Delete failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteMediaWithSync(mediaId: String): Result<Unit> {
        return try {
            val firestoreResult = mediaFirestoreDataSource.deleteMedia(mediaId)
            if (firestoreResult.isSuccess) {
                mediaDao.deleteById(mediaId)
                Result.success(Unit)
            } else {
                Result.failure(firestoreResult.exceptionOrNull() ?: Exception("Delete failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}