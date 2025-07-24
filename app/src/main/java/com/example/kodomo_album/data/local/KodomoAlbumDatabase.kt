package com.example.kodomo_album.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.kodomo_album.data.local.dao.*
import com.example.kodomo_album.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        ChildEntity::class,
        MediaEntity::class,
        DiaryEntity::class,
        GrowthRecordEntity::class,
        MilestoneEntity::class,
        EventEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(DatabaseConverters::class)
abstract class KodomoAlbumDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun childDao(): ChildDao
    abstract fun mediaDao(): MediaDao
    abstract fun diaryDao(): DiaryDao
    abstract fun growthRecordDao(): GrowthRecordDao
    abstract fun milestoneDao(): MilestoneDao
    abstract fun eventDao(): EventDao
}