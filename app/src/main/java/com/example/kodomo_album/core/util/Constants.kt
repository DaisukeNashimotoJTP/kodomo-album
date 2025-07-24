package com.example.kodomo_album.core.util

object Constants {
    
    // Database
    const val DATABASE_NAME = "kodomo_album_database"
    
    // Firebase Collections
    const val USERS_COLLECTION = "users"
    const val CHILDREN_COLLECTION = "children"
    const val MEDIA_COLLECTION = "media"
    const val DIARY_COLLECTION = "diary"
    const val GROWTH_RECORDS_COLLECTION = "growth_records"
    const val DEVELOPMENT_RECORDS_COLLECTION = "development_records"
    const val EVENTS_COLLECTION = "events"
    
    // Firebase Storage
    const val PROFILE_IMAGES_PATH = "profile_images"
    const val MEDIA_IMAGES_PATH = "media_images"
    const val MEDIA_VIDEOS_PATH = "media_videos"
    
    // Preferences
    const val PREFERENCES_NAME = "kodomo_album_preferences"
    const val FIRST_TIME_LAUNCH = "first_time_launch"
    const val CURRENT_USER_ID = "current_user_id"
    const val SELECTED_CHILD_ID = "selected_child_id"
    
    // Network
    const val NETWORK_TIMEOUT = 30L
    
    // Error Messages
    const val GENERIC_ERROR_MESSAGE = "予期しないエラーが発生しました"
    const val NETWORK_ERROR_MESSAGE = "ネットワーク接続を確認してください"
    const val AUTH_ERROR_MESSAGE = "認証に失敗しました"
}