package com.example.kodomoalbum.domain.usecase.sharing

import com.example.kodomoalbum.domain.model.SharedContent
import com.example.kodomoalbum.domain.repository.SharingRepository
import javax.inject.Inject

class GetSharedContentUseCase @Inject constructor(
    private val sharingRepository: SharingRepository
) {
    
    suspend operator fun invoke(): List<SharedContent> {
        return try {
            sharingRepository.getSharedContent()
        } catch (e: Exception) {
            emptyList()
        }
    }
}