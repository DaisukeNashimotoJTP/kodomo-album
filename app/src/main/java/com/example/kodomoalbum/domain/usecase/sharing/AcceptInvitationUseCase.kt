package com.example.kodomoalbum.domain.usecase.sharing

import com.example.kodomoalbum.domain.repository.SharingRepository
import javax.inject.Inject

class AcceptInvitationUseCase @Inject constructor(
    private val sharingRepository: SharingRepository
) {
    
    suspend operator fun invoke(invitationId: String): Result<Unit> {
        if (invitationId.isBlank()) {
            return Result.failure(IllegalArgumentException("Invitation ID cannot be empty"))
        }
        
        return sharingRepository.acceptInvitation(invitationId)
    }
}