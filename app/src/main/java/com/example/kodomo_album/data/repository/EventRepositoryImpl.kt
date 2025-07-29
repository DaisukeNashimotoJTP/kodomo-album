package com.example.kodomo_album.data.repository

import com.example.kodomo_album.core.util.Resource
import com.example.kodomo_album.data.local.dao.EventDao
import com.example.kodomo_album.data.local.entity.EventEntity
import com.example.kodomo_album.domain.model.Event
import com.example.kodomo_album.domain.model.EventType
import com.example.kodomo_album.domain.repository.EventRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val eventDao: EventDao,
    private val gson: Gson
) : BaseRepository(), EventRepository {

    override suspend fun createEvent(event: Event): Resource<Event> {
        return safeApiCall {
            val entity = eventToEntity(event)
            eventDao.insertEvent(entity)
            event
        }
    }

    override suspend fun getEvents(childId: String): Flow<List<Event>> {
        return eventDao.getEventsByChildId(childId).map { entities ->
            entities.map { entityToEvent(it) }
        }
    }

    override suspend fun updateEvent(event: Event): Resource<Event> {
        return safeApiCall {
            val entity = eventToEntity(event)
            eventDao.updateEvent(entity)
            event
        }
    }

    override suspend fun deleteEvent(eventId: String): Resource<Unit> {
        return safeApiCall {
            eventDao.deleteEventById(eventId)
        }
    }

    override suspend fun getEventById(eventId: String): Resource<Event> {
        return safeApiCall {
            val entity = eventDao.getEventById(eventId)
                ?: throw Exception("イベントが見つかりません")
            entityToEvent(entity)
        }
    }

    private fun eventToEntity(event: Event): EventEntity {
        return EventEntity(
            id = event.id,
            childId = event.childId,
            title = event.title,
            description = event.description,
            eventDate = event.eventDate.toEpochDay() * 24 * 60 * 60 * 1000, // 日付をUnix timestampに変換
            mediaIds = gson.toJson(event.mediaIds),
            eventType = event.eventType.name,
            createdAt = event.createdAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            updatedAt = event.updatedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            isSynced = false
        )
    }

    private fun entityToEvent(entity: EventEntity): Event {
        val mediaIds = try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson<List<String>>(entity.mediaIds, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        return Event(
            id = entity.id,
            childId = entity.childId,
            title = entity.title,
            description = entity.description,
            eventDate = LocalDate.ofEpochDay(entity.eventDate / (24 * 60 * 60 * 1000)),
            mediaIds = mediaIds,
            eventType = EventType.valueOf(entity.eventType),
            createdAt = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(entity.createdAt),
                ZoneId.systemDefault()
            ),
            updatedAt = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(entity.updatedAt),
                ZoneId.systemDefault()
            )
        )
    }
}