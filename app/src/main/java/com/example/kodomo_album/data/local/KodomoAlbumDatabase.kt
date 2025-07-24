package com.example.kodomo_album.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.kodomo_album.data.local.dao.UserDao
import com.example.kodomo_album.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class KodomoAlbumDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}