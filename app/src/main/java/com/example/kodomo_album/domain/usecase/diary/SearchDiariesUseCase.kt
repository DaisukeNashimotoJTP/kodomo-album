package com.example.kodomo_album.domain.usecase.diary

import com.example.kodomo_album.domain.model.Diary
import com.example.kodomo_album.domain.repository.DiaryRepository
import com.example.kodomo_album.core.util.Resource
import javax.inject.Inject

class SearchDiariesUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository
) {
    suspend operator fun invoke(childId: String, keyword: String): Resource<List<Diary>> {
        if (keyword.isBlank()) {
            return diaryRepository.getDiaries(childId)
        }
        return diaryRepository.searchDiaries(childId, keyword.trim())
    }
}