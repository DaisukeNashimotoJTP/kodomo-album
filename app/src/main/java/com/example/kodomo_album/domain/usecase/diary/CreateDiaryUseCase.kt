package com.example.kodomo_album.domain.usecase.diary

import com.example.kodomo_album.domain.model.Diary
import com.example.kodomo_album.domain.repository.DiaryRepository
import com.example.kodomo_album.core.util.Resource
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

class CreateDiaryUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository
) {
    suspend operator fun invoke(
        childId: String,
        title: String,
        content: String,
        mediaIds: List<String> = emptyList(),
        date: java.time.LocalDate = java.time.LocalDate.now()
    ): Resource<Diary> {
        val diary = Diary(
            id = UUID.randomUUID().toString(),
            childId = childId,
            title = title,
            content = content,
            mediaIds = mediaIds,
            date = date,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        return diaryRepository.createDiary(diary)
    }
}