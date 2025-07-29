package com.example.kodomo_album.di

import com.example.kodomo_album.data.repository.AuthRepository
import com.example.kodomo_album.data.repository.AuthRepositoryImpl
import com.example.kodomo_album.data.repository.ChildRepository
import com.example.kodomo_album.data.repository.ChildRepositoryImpl
import com.example.kodomo_album.data.repository.DiaryRepositoryImpl
import com.example.kodomo_album.data.repository.EventRepositoryImpl
import com.example.kodomo_album.data.repository.GrowthRepositoryImpl
import com.example.kodomo_album.data.repository.MediaRepositoryImpl
import com.example.kodomo_album.data.repository.MilestoneRepositoryImpl
import com.example.kodomo_album.domain.repository.DiaryRepository
import com.example.kodomo_album.domain.repository.EventRepository
import com.example.kodomo_album.domain.repository.GrowthRepository
import com.example.kodomo_album.domain.repository.MediaRepository
import com.example.kodomo_album.domain.repository.MilestoneRepository
import com.example.kodomoalbum.data.repository.SharingRepositoryImpl
import com.example.kodomoalbum.domain.repository.SharingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindChildRepository(
        childRepositoryImpl: ChildRepositoryImpl
    ): ChildRepository
    
    @Binds
    @Singleton
    abstract fun bindMediaRepository(
        mediaRepositoryImpl: MediaRepositoryImpl
    ): MediaRepository
    
    @Binds
    @Singleton
    abstract fun bindDiaryRepository(
        diaryRepositoryImpl: DiaryRepositoryImpl
    ): DiaryRepository
    
    @Binds
    @Singleton
    abstract fun bindSharingRepository(
        sharingRepositoryImpl: SharingRepositoryImpl
    ): SharingRepository
    
    @Binds
    @Singleton
    abstract fun bindGrowthRepository(
        growthRepositoryImpl: GrowthRepositoryImpl
    ): GrowthRepository
    
    @Binds
    @Singleton
    abstract fun bindMilestoneRepository(
        milestoneRepositoryImpl: MilestoneRepositoryImpl
    ): MilestoneRepository
    
    @Binds
    @Singleton
    abstract fun bindEventRepository(
        eventRepositoryImpl: EventRepositoryImpl
    ): EventRepository
}