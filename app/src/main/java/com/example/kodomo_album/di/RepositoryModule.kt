package com.example.kodomo_album.di

import com.example.kodomo_album.data.repository.AuthRepository
import com.example.kodomo_album.data.repository.AuthRepositoryImpl
import com.example.kodomo_album.data.repository.ChildRepository
import com.example.kodomo_album.data.repository.ChildRepositoryImpl
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
}