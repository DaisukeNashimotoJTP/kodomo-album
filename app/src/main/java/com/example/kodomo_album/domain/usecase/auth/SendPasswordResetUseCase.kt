package com.example.kodomo_album.domain.usecase.auth

import com.example.kodomo_album.core.util.Resource
import com.example.kodomo_album.data.repository.AuthRepository
import javax.inject.Inject

class SendPasswordResetUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): Resource<Unit> {
        if (email.isBlank()) {
            return Resource.Error("メールアドレスを入力してください")
        }
        
        if (!isValidEmail(email)) {
            return Resource.Error("有効なメールアドレスを入力してください")
        }
        
        return authRepository.sendPasswordResetEmail(email)
    }
    
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}