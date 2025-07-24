package com.example.kodomo_album.domain.usecase.media

import com.example.kodomo_album.domain.model.Media
import com.example.kodomo_album.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GetMediaListUseCase @Inject constructor(
    private val mediaRepository: MediaRepository
) {
    
    suspend operator fun invoke(
        childId: String,
        dateRange: Pair<LocalDate, LocalDate>? = null
    ): Flow<List<Media>> {
        return mediaRepository.getMediaList(childId, dateRange)
    }
}