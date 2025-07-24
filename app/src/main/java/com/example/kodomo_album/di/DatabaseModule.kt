package com.example.kodomo_album.di

import android.content.Context
import androidx.room.Room
import com.example.kodomo_album.core.util.Constants
import com.example.kodomo_album.data.local.KodomoAlbumDatabase
import com.example.kodomo_album.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideKodomoAlbumDatabase(@ApplicationContext context: Context): KodomoAlbumDatabase {
        return Room.databaseBuilder(
            context,
            KodomoAlbumDatabase::class.java,
            Constants.DATABASE_NAME
        ).build()
    }

    @Provides
    fun provideUserDao(database: KodomoAlbumDatabase): UserDao {
        return database.userDao()
    }
}