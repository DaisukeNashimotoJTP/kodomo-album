package com.example.kodomo_album.domain.repository

import com.example.kodomo_album.core.util.Resource
import com.example.kodomo_album.domain.model.Media
import com.example.kodomo_album.domain.model.MediaMetadata
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.time.LocalDate

interface MediaRepository {
    suspend fun uploadPhoto(file: File, metadata: MediaMetadata): Resource<Media>
    suspend fun uploadVideo(file: File, metadata: MediaMetadata): Resource<Media>
    suspend fun getMediaList(childId: String, dateRange: Pair<LocalDate, LocalDate>? = null): Flow<List<Media>>
    suspend fun deleteMedia(mediaId: String): Resource<Unit>
    suspend fun getMediaById(mediaId: String): Resource<Media>
    suspend fun updateMediaCaption(mediaId: String, caption: String): Resource<Media>
}