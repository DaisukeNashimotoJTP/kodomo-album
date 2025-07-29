package com.example.kodomoalbum.domain.usecase.sharing

import com.example.kodomoalbum.domain.model.Invitation
import com.example.kodomo_album.data.repository.AuthRepository
import com.example.kodomoalbum.domain.repository.SharingRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetPendingInvitationsUseCase @Inject constructor(
    private val sharingRepository: SharingRepository,
    private val authRepository: AuthRepository
) {
    
    suspend operator fun invoke(): List<Invitation> {
        val currentUser = authRepository.getCurrentUser().first()
        return if (currentUser != null) {
            sharingRepository.getPendingInvitations(currentUser.email)
        } else {
            emptyList()
        }
    }
}