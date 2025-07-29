package com.example.kodomo_album.domain.repository

import com.example.kodomo_album.core.util.Resource
import com.example.kodomo_album.domain.model.Event
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    suspend fun createEvent(event: Event): Resource<Event>
    suspend fun getEvents(childId: String): Flow<List<Event>>
    suspend fun updateEvent(event: Event): Resource<Event>
    suspend fun deleteEvent(eventId: String): Resource<Unit>
    suspend fun getEventById(eventId: String): Resource<Event>
}