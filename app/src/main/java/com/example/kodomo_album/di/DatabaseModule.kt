package com.example.kodomo_album.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.kodomo_album.core.util.Constants
import com.example.kodomo_album.data.local.KodomoAlbumDatabase
import com.example.kodomo_album.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Create children table
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `children` (
                    `id` TEXT NOT NULL,
                    `userId` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `birthDate` INTEGER NOT NULL,
                    `gender` TEXT NOT NULL,
                    `profileImageUrl` TEXT,
                    `createdAt` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`userId`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
            """.trimIndent())
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_children_userId` ON `children` (`userId`)")

            // Create media table
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `media` (
                    `id` TEXT NOT NULL,
                    `childId` TEXT NOT NULL,
                    `type` TEXT NOT NULL,
                    `url` TEXT NOT NULL,
                    `thumbnailUrl` TEXT,
                    `caption` TEXT,
                    `takenAt` INTEGER NOT NULL,
                    `uploadedAt` INTEGER NOT NULL,
                    `isUploaded` INTEGER NOT NULL DEFAULT 0,
                    `localPath` TEXT,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`childId`) REFERENCES `children`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
            """.trimIndent())
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_media_childId` ON `media` (`childId`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_media_takenAt` ON `media` (`takenAt`)")

            // Create diaries table
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `diaries` (
                    `id` TEXT NOT NULL,
                    `childId` TEXT NOT NULL,
                    `title` TEXT NOT NULL,
                    `content` TEXT NOT NULL,
                    `mediaIds` TEXT NOT NULL,
                    `date` INTEGER NOT NULL,
                    `createdAt` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL,
                    `isSynced` INTEGER NOT NULL DEFAULT 0,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`childId`) REFERENCES `children`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
            """.trimIndent())
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_diaries_childId` ON `diaries` (`childId`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_diaries_date` ON `diaries` (`date`)")

            // Create growth_records table
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `growth_records` (
                    `id` TEXT NOT NULL,
                    `childId` TEXT NOT NULL,
                    `height` REAL,
                    `weight` REAL,
                    `headCircumference` REAL,
                    `recordedAt` INTEGER NOT NULL,
                    `notes` TEXT,
                    `createdAt` INTEGER NOT NULL,
                    `isSynced` INTEGER NOT NULL DEFAULT 0,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`childId`) REFERENCES `children`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
            """.trimIndent())
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_growth_records_childId` ON `growth_records` (`childId`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_growth_records_recordedAt` ON `growth_records` (`recordedAt`)")

            // Create milestones table
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `milestones` (
                    `id` TEXT NOT NULL,
                    `childId` TEXT NOT NULL,
                    `type` TEXT NOT NULL,
                    `title` TEXT NOT NULL,
                    `description` TEXT NOT NULL,
                    `achievedAt` INTEGER NOT NULL,
                    `mediaIds` TEXT NOT NULL,
                    `createdAt` INTEGER NOT NULL,
                    `isSynced` INTEGER NOT NULL DEFAULT 0,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`childId`) REFERENCES `children`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
            """.trimIndent())
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_milestones_childId` ON `milestones` (`childId`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_milestones_achievedAt` ON `milestones` (`achievedAt`)")

            // Create events table
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `events` (
                    `id` TEXT NOT NULL,
                    `childId` TEXT NOT NULL,
                    `title` TEXT NOT NULL,
                    `description` TEXT NOT NULL,
                    `eventDate` INTEGER NOT NULL,
                    `mediaIds` TEXT NOT NULL,
                    `eventType` TEXT NOT NULL,
                    `createdAt` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL,
                    `isSynced` INTEGER NOT NULL DEFAULT 0,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`childId`) REFERENCES `children`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
            """.trimIndent())
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_events_childId` ON `events` (`childId`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_events_eventDate` ON `events` (`eventDate`)")
        }
    }

    @Provides
    @Singleton
    fun provideKodomoAlbumDatabase(@ApplicationContext context: Context): KodomoAlbumDatabase {
        return Room.databaseBuilder(
            context,
            KodomoAlbumDatabase::class.java,
            Constants.DATABASE_NAME
        )
        .addMigrations(MIGRATION_1_2)
        .build()
    }

    @Provides
    fun provideUserDao(database: KodomoAlbumDatabase): UserDao = database.userDao()

    @Provides
    fun provideChildDao(database: KodomoAlbumDatabase): ChildDao = database.childDao()

    @Provides
    fun provideMediaDao(database: KodomoAlbumDatabase): MediaDao = database.mediaDao()

    @Provides
    fun provideDiaryDao(database: KodomoAlbumDatabase): DiaryDao = database.diaryDao()

    @Provides
    fun provideGrowthRecordDao(database: KodomoAlbumDatabase): GrowthRecordDao = database.growthRecordDao()

    @Provides
    fun provideMilestoneDao(database: KodomoAlbumDatabase): MilestoneDao = database.milestoneDao()

    @Provides
    fun provideEventDao(database: KodomoAlbumDatabase): EventDao = database.eventDao()
}