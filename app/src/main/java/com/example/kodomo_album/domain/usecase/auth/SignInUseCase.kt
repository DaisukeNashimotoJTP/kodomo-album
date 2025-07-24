package com.example.kodomo_album.domain.usecase.auth

import com.example.kodomo_album.core.util.Resource
import com.example.kodomo_album.data.repository.AuthRepository
import com.example.kodomo_album.domain.model.User
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Resource<User> {
        if (email.isBlank()) {
            return Resource.Error("メールアドレスを入力してください")
        }
        
        if (password.isBlank()) {
            return Resource.Error("パスワードを入力してください")
        }
        
        if (!isValidEmail(email)) {
            return Resource.Error("有効なメールアドレスを入力してください")
        }
        
        return authRepository.signInWithEmailAndPassword(email, password)
    }
    
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}