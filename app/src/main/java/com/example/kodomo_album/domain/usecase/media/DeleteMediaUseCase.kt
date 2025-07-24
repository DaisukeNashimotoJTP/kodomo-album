package com.example.kodomo_album.domain.usecase.media

import com.example.kodomo_album.core.util.Resource
import com.example.kodomo_album.domain.repository.MediaRepository
import javax.inject.Inject

class DeleteMediaUseCase @Inject constructor(
    private val mediaRepository: MediaRepository
) {
    
    suspend operator fun invoke(mediaId: String): Resource<Unit> {
        return try {
            mediaRepository.deleteMedia(mediaId)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "メディアの削除に失敗しました")
        }
    }
}