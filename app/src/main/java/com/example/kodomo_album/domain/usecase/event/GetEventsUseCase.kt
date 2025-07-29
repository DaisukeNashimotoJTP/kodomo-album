package com.example.kodomo_album.domain.usecase.event

import com.example.kodomo_album.domain.model.Event
import com.example.kodomo_album.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetEventsUseCase @Inject constructor(
    private val repository: EventRepository
) {
    suspend operator fun invoke(childId: String): Flow<List<Event>> {
        return repository.getEvents(childId)
    }
}