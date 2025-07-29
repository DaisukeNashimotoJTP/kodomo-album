package com.example.kodomo_album.domain.usecase.event

import com.example.kodomo_album.core.util.Resource
import com.example.kodomo_album.domain.model.Event
import com.example.kodomo_album.domain.repository.EventRepository
import javax.inject.Inject

class CreateEventUseCase @Inject constructor(
    private val repository: EventRepository
) {
    suspend operator fun invoke(event: Event): Resource<Event> {
        return repository.createEvent(event)
    }
}