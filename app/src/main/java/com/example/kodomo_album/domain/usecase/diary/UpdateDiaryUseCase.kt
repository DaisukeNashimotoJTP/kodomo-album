package com.example.kodomo_album.domain.usecase.diary

import com.example.kodomo_album.domain.model.Diary
import com.example.kodomo_album.domain.repository.DiaryRepository
import com.example.kodomo_album.core.util.Resource
import java.time.LocalDateTime
import javax.inject.Inject

class UpdateDiaryUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository
) {
    suspend operator fun invoke(
        diary: Diary,
        newTitle: String? = null,
        newContent: String? = null,
        newMediaIds: List<String>? = null
    ): Resource<Diary> {
        val updatedDiary = diary.copy(
            title = newTitle ?: diary.title,
            content = newContent ?: diary.content,
            mediaIds = newMediaIds ?: diary.mediaIds,
            updatedAt = LocalDateTime.now()
        )
        
        return diaryRepository.updateDiary(updatedDiary)
    }
}