package com.example.kodomo_album.data.repository

import com.example.kodomo_album.core.util.Resource
import com.example.kodomo_album.data.firebase.datasource.MediaFirestoreDataSource
import com.example.kodomo_album.data.firebase.datasource.MediaStorageDataSource
import com.example.kodomo_album.data.local.dao.MediaDao
import com.example.kodomo_album.data.mapper.*
import java.time.format.DateTimeFormatter
import com.example.kodomo_album.domain.model.Media
import com.example.kodomo_album.domain.model.MediaMetadata
import com.example.kodomo_album.domain.model.MediaType
import com.example.kodomo_album.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

class MediaRepositoryImpl @Inject constructor(
    private val mediaDao: MediaDao,
    private val mediaFirestoreDataSource: MediaFirestoreDataSource,
    private val mediaStorageDataSource: MediaStorageDataSource
) : MediaRepository, BaseRepository() {

    override suspend fun uploadPhoto(file: File, metadata: MediaMetadata): Resource<Media> {
        return safeApiCall {
            val mediaId = UUID.randomUUID().toString()
            val fileName = "${mediaId}.jpg"
            
            val imageUrl = mediaStorageDataSource.uploadImage(
                childId = metadata.childId,
                file = file,
                fileName = fileName
            )
            
            val media = Media(
                id = mediaId,
                childId = metadata.childId,
                type = metadata.type,
                url = imageUrl,
                thumbnailUrl = imageUrl,
                caption = metadata.caption,
                takenAt = metadata.takenAt,
                uploadedAt = LocalDateTime.now()
            )
            
            mediaDao.insertMedia(media.toEntity())
            
            val firebaseMedia = media.toFirebaseMedia()
            mediaFirestoreDataSource.createMedia(firebaseMedia)
            
            media
        }
    }

    override suspend fun uploadVideo(file: File, metadata: MediaMetadata): Resource<Media> {
        return safeApiCall {
            val mediaId = UUID.randomUUID().toString()
            val fileName = "${mediaId}.mp4"
            
            val videoUrl = mediaStorageDataSource.uploadVideo(
                childId = metadata.childId,
                file = file,
                fileName = fileName
            )
            
            val media = Media(
                id = mediaId,
                childId = metadata.childId,
                type = MediaType.VIDEO,
                url = videoUrl,
                thumbnailUrl = null,
                caption = metadata.caption,
                takenAt = metadata.takenAt,
                uploadedAt = LocalDateTime.now()
            )
            
            mediaDao.insertMedia(media.toEntity())
            
            val firebaseMedia = media.toFirebaseMedia()
            mediaFirestoreDataSource.createMedia(firebaseMedia)
            
            media
        }
    }

    override suspend fun getMediaList(
        childId: String,
        dateRange: Pair<LocalDate, LocalDate>?
    ): Flow<List<Media>> {
        return if (dateRange != null) {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            mediaDao.getMediaByChildIdAndDateRange(
                childId = childId,
                startDate = dateRange.first.format(formatter),
                endDate = dateRange.second.format(formatter)
            ).map { entities -> entities.map { it.toDomain() } }
        } else {
            mediaDao.getMediaByChildId(childId)
                .map { entities -> entities.map { it.toDomain() } }
        }
    }

    override suspend fun deleteMedia(mediaId: String): Resource<Unit> {
        return safeApiCall {
            val media = mediaDao.getMediaById(mediaId)?.toDomain()
            media?.let {
                mediaStorageDataSource.deleteFile(it.url)
                it.thumbnailUrl?.let { thumbnailUrl ->
                    mediaStorageDataSource.deleteFile(thumbnailUrl)
                }
                
                mediaDao.deleteMedia(mediaId)
                mediaFirestoreDataSource.deleteMedia(mediaId)
            }
            Unit
        }
    }

    override suspend fun getMediaById(mediaId: String): Resource<Media> {
        return safeApiCall {
            val entity = mediaDao.getMediaById(mediaId)
            entity?.toDomain() ?: throw Exception("Media not found")
        }
    }

    override suspend fun updateMediaCaption(mediaId: String, caption: String): Resource<Media> {
        return safeApiCall {
            mediaDao.updateMediaCaption(mediaId, caption)
            
            val updatedEntity = mediaDao.getMediaById(mediaId)
            val updatedMedia = updatedEntity?.toDomain() ?: throw Exception("Media not found")
            
            val firebaseMedia = updatedMedia.toFirebaseMedia()
            mediaFirestoreDataSource.updateMedia(firebaseMedia)
            
            updatedMedia
        }
    }
}