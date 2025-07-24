package com.example.kodomo_album.domain.usecase.media

import android.content.Context
import com.example.kodomo_album.core.util.ImageUtils
import com.example.kodomo_album.core.util.Resource
import com.example.kodomo_album.domain.model.Media
import com.example.kodomo_album.domain.model.MediaMetadata
import com.example.kodomo_album.domain.model.MediaType
import com.example.kodomo_album.domain.repository.MediaRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class UploadMediaUseCase @Inject constructor(
    private val mediaRepository: MediaRepository,
    @ApplicationContext private val context: Context
) {
    
    suspend operator fun invoke(
        file: File,
        metadata: MediaMetadata
    ): Resource<Media> = withContext(Dispatchers.IO) {
        
        val processedFile = when (metadata.type) {
            MediaType.PHOTO, MediaType.ECHO -> {
                ImageUtils.compressImage(context, android.net.Uri.fromFile(file)) ?: file
            }
            MediaType.VIDEO -> {
                file
            }
        }
        
        return@withContext when (metadata.type) {
            MediaType.VIDEO -> {
                mediaRepository.uploadVideo(processedFile, metadata)
            }
            else -> {
                mediaRepository.uploadPhoto(processedFile, metadata)
            }
        }
    }
}