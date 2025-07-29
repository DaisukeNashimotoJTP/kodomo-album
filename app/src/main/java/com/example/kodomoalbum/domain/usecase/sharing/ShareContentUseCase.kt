package com.example.kodomoalbum.domain.usecase.sharing

import com.example.kodomoalbum.domain.repository.SharingRepository
import javax.inject.Inject

class ShareContentUseCase @Inject constructor(
    private val sharingRepository: SharingRepository
) {
    
    suspend operator fun invoke(
        contentId: String,
        contentType: String,
        targetUsers: List<String>
    ): Result<Unit> {
        if (contentId.isBlank()) {
            return Result.failure(IllegalArgumentException("Content ID cannot be empty"))
        }
        
        if (contentType.isBlank()) {
            return Result.failure(IllegalArgumentException("Content type cannot be empty"))
        }
        
        if (targetUsers.isEmpty()) {
            return Result.failure(IllegalArgumentException("Target users cannot be empty"))
        }
        
        return sharingRepository.shareContent(contentId, contentType, targetUsers)
    }
}