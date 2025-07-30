package com.example.kodomoalbum.di

import com.example.kodomoalbum.data.repository.DashboardRepositoryImpl
import com.example.kodomoalbum.data.repository.ExportRepositoryImpl
import com.example.kodomoalbum.data.repository.SearchRepositoryImpl
import com.example.kodomoalbum.domain.repository.DashboardRepository
import com.example.kodomoalbum.domain.repository.ExportRepository
import com.example.kodomoalbum.domain.repository.SearchRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class Phase9Module {
    
    @Binds
    @Singleton
    abstract fun bindSearchRepository(
        searchRepositoryImpl: SearchRepositoryImpl
    ): SearchRepository
    
    @Binds
    @Singleton
    abstract fun bindDashboardRepository(
        dashboardRepositoryImpl: DashboardRepositoryImpl
    ): DashboardRepository
    
    @Binds
    @Singleton
    abstract fun bindExportRepository(
        exportRepositoryImpl: ExportRepositoryImpl
    ): ExportRepository
}