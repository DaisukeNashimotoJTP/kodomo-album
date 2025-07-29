package com.example.kodomo_album.domain.usecase.diary

import com.example.kodomo_album.domain.model.Diary
import com.example.kodomo_album.domain.repository.DiaryRepository
import com.example.kodomo_album.core.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDiariesUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository
) {
    suspend operator fun invoke(childId: String): Resource<List<Diary>> {
        return diaryRepository.getDiaries(childId)
    }
    
    fun getFlow(childId: String): Flow<List<Diary>> {
        return diaryRepository.getDiariesFlow(childId)
    }
}