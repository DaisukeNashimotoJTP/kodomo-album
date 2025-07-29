package com.example.kodomoalbum.domain.usecase.sharing

import com.example.kodomoalbum.domain.model.Invitation
import com.example.kodomoalbum.domain.repository.SharingRepository
import javax.inject.Inject

class InvitePartnerUseCase @Inject constructor(
    private val sharingRepository: SharingRepository
) {
    
    suspend operator fun invoke(email: String, familyName: String): Result<Invitation> {
        if (email.isBlank()) {
            return Result.failure(IllegalArgumentException("Email cannot be empty"))
        }
        
        if (!isValidEmail(email)) {
            return Result.failure(IllegalArgumentException("Invalid email format"))
        }
        
        if (familyName.isBlank()) {
            return Result.failure(IllegalArgumentException("Family name cannot be empty"))
        }
        
        return sharingRepository.invitePartner(email, familyName)
    }
    
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}