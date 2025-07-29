package com.example.kodomo_album.data.repository

import com.example.kodomo_album.domain.repository.DiaryRepository
import com.example.kodomo_album.domain.model.Diary
import com.example.kodomo_album.data.local.dao.DiaryDao
import com.example.kodomo_album.data.firebase.datasource.DiaryFirestoreDataSource
import com.example.kodomo_album.data.mapper.FirebaseMapper
import com.example.kodomo_album.core.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiaryRepositoryImpl @Inject constructor(
    private val diaryDao: DiaryDao,
    private val diaryFirestoreDataSource: DiaryFirestoreDataSource,
    private val firebaseMapper: FirebaseMapper
) : DiaryRepository {
    
    override suspend fun createDiary(diary: Diary): Resource<Diary> {
        return try {
            // ローカルに保存
            val diaryEntity = firebaseMapper.diaryToEntity(diary)
            diaryDao.insertDiary(diaryEntity)
            
            // Firestoreに保存を試行
            try {
                val firebaseDiary = firebaseMapper.diaryToFirebase(diary)
                diaryFirestoreDataSource.createDiary(firebaseDiary)
                // 同期完了をマーク
                diaryDao.markAsSynced(diary.id)
            } catch (e: Exception) {
                // オフライン時はローカル保存のみ
            }
            
            Resource.Success(diary)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "日記の作成に失敗しました")
        }
    }
    
    override suspend fun getDiaries(childId: String): Resource<List<Diary>> {
        return try {
            val localDiaries = diaryDao.getDiariesByChildId(childId)
            val diaries = localDiaries.map { firebaseMapper.diaryEntityToDomain(it) }
            Resource.Success(diaries)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "日記の取得に失敗しました")
        }
    }
    
    override fun getDiariesFlow(childId: String): Flow<List<Diary>> {
        return diaryDao.getDiariesByChildIdFlow(childId)
            .map { entities -> entities.map { firebaseMapper.diaryEntityToDomain(it) } }
    }
    
    override suspend fun getDiariesByDateRange(
        childId: String, 
        startDate: LocalDate, 
        endDate: LocalDate
    ): Resource<List<Diary>> {
        return try {
            val startTimestamp = startDate.atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000
            val endTimestamp = endDate.atTime(23, 59, 59).toEpochSecond(ZoneOffset.UTC) * 1000
            
            val localDiaries = diaryDao.getDiariesByDateRange(childId, startTimestamp, endTimestamp)
            val diaries = localDiaries.map { entities -> entities.map { firebaseMapper.diaryEntityToDomain(it) } }
            
            // Flowを一度だけ収集してResourceに変換
            val result = mutableListOf<Diary>()
            diaries.collect { result.addAll(it) }
            
            Resource.Success(result.toList())
        } catch (e: Exception) {
            Resource.Error(e.message ?: "日記の取得に失敗しました")
        }
    }
    
    override suspend fun getDiaryById(diaryId: String): Resource<Diary> {
        return try {
            val diaryEntity = diaryDao.getDiaryById(diaryId)
            if (diaryEntity != null) {
                val diary = firebaseMapper.diaryEntityToDomain(diaryEntity)
                Resource.Success(diary)
            } else {
                Resource.Error("日記が見つかりません")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "日記の取得に失敗しました")
        }
    }
    
    override suspend fun updateDiary(diary: Diary): Resource<Diary> {
        return try {
            // ローカルを更新
            val diaryEntity = firebaseMapper.diaryToEntity(diary).copy(isSynced = false)
            diaryDao.update(diaryEntity)
            
            // Firestoreを更新を試行
            try {
                val firebaseDiary = firebaseMapper.diaryToFirebase(diary)
                diaryFirestoreDataSource.updateDiary(firebaseDiary)
                diaryDao.markAsSynced(diary.id)
            } catch (e: Exception) {
                // オフライン時はローカル更新のみ
            }
            
            Resource.Success(diary)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "日記の更新に失敗しました")
        }
    }
    
    override suspend fun deleteDiary(diaryId: String): Resource<Unit> {
        return try {
            // ローカルから削除
            diaryDao.deleteById(diaryId)
            
            // Firestoreから削除を試行
            try {
                diaryFirestoreDataSource.deleteDiary(diaryId)
            } catch (e: Exception) {
                // オフライン時はローカル削除のみ
            }
            
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "日記の削除に失敗しました")
        }
    }
    
    override suspend fun searchDiaries(childId: String, keyword: String): Resource<List<Diary>> {
        return try {
            val localDiaries = diaryDao.searchDiaries(childId, keyword)
            val diaries = localDiaries.map { firebaseMapper.diaryEntityToDomain(it) }
            Resource.Success(diaries)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "日記の検索に失敗しました")
        }
    }
}