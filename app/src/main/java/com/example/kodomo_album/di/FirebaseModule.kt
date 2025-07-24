package com.example.kodomo_album.di

import android.content.Context
import com.example.kodomo_album.data.firebase.*
import com.example.kodomo_album.data.firebase.datasource.*
import com.example.kodomo_album.data.local.dao.*
import com.example.kodomo_album.data.mapper.FirebaseMapper
import com.example.kodomo_album.data.repository.SyncRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance().apply {
            // オフライン永続化を有効にする
            val settings = com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
            firestoreSettings = settings
        }
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }

    @Provides
    @Singleton
    fun provideChildFirestoreDataSource(
        firestore: FirebaseFirestore
    ): ChildFirestoreDataSource {
        return ChildFirestoreDataSource(firestore)
    }

    @Provides
    @Singleton
    fun provideDiaryFirestoreDataSource(
        firestore: FirebaseFirestore
    ): DiaryFirestoreDataSource {
        return DiaryFirestoreDataSource(firestore)
    }

    @Provides
    @Singleton
    fun provideMediaFirestoreDataSource(
        firestore: FirebaseFirestore
    ): MediaFirestoreDataSource {
        return MediaFirestoreDataSource(firestore)
    }
    
    @Provides
    @Singleton
    fun provideMediaStorageDataSource(
        storage: FirebaseStorage
    ): MediaStorageDataSource {
        return MediaStorageDataSource(storage)
    }

    @Provides
    @Singleton
    fun provideFirebaseMapper(): FirebaseMapper {
        return FirebaseMapper()
    }

    @Provides
    @Singleton
    fun provideFirestoreService(
        childFirestoreDataSource: ChildFirestoreDataSource,
        diaryFirestoreDataSource: DiaryFirestoreDataSource,
        mediaFirestoreDataSource: MediaFirestoreDataSource,
        childDao: ChildDao,
        diaryDao: DiaryDao,
        mediaDao: MediaDao,
        firebaseMapper: FirebaseMapper
    ): FirestoreService {
        return FirestoreService(
            childFirestoreDataSource,
            diaryFirestoreDataSource,
            mediaFirestoreDataSource,
            childDao,
            diaryDao,
            mediaDao,
            firebaseMapper
        )
    }

    @Provides
    @Singleton
    fun provideSyncService(
        firestoreService: FirestoreService,
        childDao: ChildDao,
        diaryDao: DiaryDao,
        mediaDao: MediaDao
    ): SyncService {
        return SyncService(firestoreService, childDao, diaryDao, mediaDao)
    }

    @Provides
    @Singleton
    fun provideOfflineManager(
        @ApplicationContext context: Context,
        syncService: SyncService
    ): OfflineManager {
        return OfflineManager(context, syncService)
    }

    @Provides
    @Singleton
    fun provideSyncRepository(
        firestoreService: FirestoreService,
        syncService: SyncService,
        offlineManager: OfflineManager,
        childDao: ChildDao,
        diaryDao: DiaryDao,
        mediaDao: MediaDao
    ): SyncRepository {
        return SyncRepository(
            firestoreService,
            syncService,
            offlineManager,
            childDao,
            diaryDao,
            mediaDao
        )
    }
}