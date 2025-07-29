package com.example.kodomo_album.domain.usecase.diary

import com.example.kodomo_album.domain.repository.DiaryRepository
import com.example.kodomo_album.core.util.Resource
import javax.inject.Inject

class DeleteDiaryUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository
) {
    suspend operator fun invoke(diaryId: String): Resource<Unit> {
        return diaryRepository.deleteDiary(diaryId)
    }
}