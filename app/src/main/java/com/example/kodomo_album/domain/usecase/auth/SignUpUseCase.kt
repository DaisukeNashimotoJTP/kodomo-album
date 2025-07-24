package com.example.kodomo_album.domain.usecase.auth

import com.example.kodomo_album.core.util.Resource
import com.example.kodomo_album.data.repository.AuthRepository
import com.example.kodomo_album.domain.model.User
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String, displayName: String): Resource<User> {
        if (email.isBlank()) {
            return Resource.Error("メールアドレスを入力してください")
        }
        
        if (password.isBlank()) {
            return Resource.Error("パスワードを入力してください")
        }
        
        if (displayName.isBlank()) {
            return Resource.Error("名前を入力してください")
        }
        
        if (!isValidEmail(email)) {
            return Resource.Error("有効なメールアドレスを入力してください")
        }
        
        if (password.length < 6) {
            return Resource.Error("パスワードは6文字以上で入力してください")
        }
        
        return authRepository.signUpWithEmailAndPassword(email, password, displayName)
    }
    
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}