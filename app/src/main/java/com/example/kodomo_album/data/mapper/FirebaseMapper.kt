package com.example.kodomo_album.data.mapper

import com.example.kodomo_album.data.firebase.models.*
import com.example.kodomo_album.data.local.entity.*
import com.example.kodomo_album.domain.model.Child
import com.example.kodomo_album.domain.model.Gender
import com.google.firebase.Timestamp
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseMapper @Inject constructor() {

    // Child mapping
    fun childEntityToFirebase(entity: ChildEntity): FirebaseChild = FirebaseChild(
        id = entity.id,
        userId = entity.userId,
        name = entity.name,
        birthDate = Timestamp(Instant.ofEpochMilli(entity.birthDate)),
        gender = entity.gender,
        profileImageUrl = entity.profileImageUrl,
        createdAt = Timestamp(Instant.ofEpochMilli(entity.createdAt)),
        updatedAt = Timestamp(Instant.ofEpochMilli(entity.updatedAt))
    )

    fun firebaseToChildEntity(firebase: FirebaseChild): ChildEntity = ChildEntity(
        id = firebase.id,
        userId = firebase.userId,
        name = firebase.name,
        birthDate = firebase.birthDate.toDate().time,
        gender = firebase.gender,
        profileImageUrl = firebase.profileImageUrl,
        createdAt = firebase.createdAt.toDate().time,
        updatedAt = firebase.updatedAt.toDate().time
    )

    // Media mapping
    fun mediaEntityToFirebase(entity: MediaEntity): FirebaseMedia = FirebaseMedia(
        id = entity.id,
        childId = entity.childId,
        type = entity.type,
        url = entity.url,
        thumbnailUrl = entity.thumbnailUrl,
        caption = entity.caption,
        takenAt = Timestamp(Instant.ofEpochMilli(entity.takenAt)),
        uploadedAt = Timestamp(Instant.ofEpochMilli(entity.uploadedAt))
    )

    fun firebaseToMediaEntity(firebase: FirebaseMedia): MediaEntity = MediaEntity(
        id = firebase.id,
        childId = firebase.childId,
        type = firebase.type,
        url = firebase.url,
        thumbnailUrl = firebase.thumbnailUrl,
        caption = firebase.caption,
        takenAt = firebase.takenAt.toDate().time,
        uploadedAt = firebase.uploadedAt.toDate().time,
        isUploaded = true,
        localPath = null
    )

    // Diary mapping
    fun diaryEntityToFirebase(entity: DiaryEntity): FirebaseDiary = FirebaseDiary(
        id = entity.id,
        childId = entity.childId,
        title = entity.title,
        content = entity.content,
        mediaIds = entity.mediaIds.split(",").filter { it.isNotEmpty() },
        date = Timestamp(Instant.ofEpochMilli(entity.date)),
        createdAt = Timestamp(Instant.ofEpochMilli(entity.createdAt)),
        updatedAt = Timestamp(Instant.ofEpochMilli(entity.updatedAt))
    )

    fun firebaseToDiaryEntity(firebase: FirebaseDiary): DiaryEntity = DiaryEntity(
        id = firebase.id,
        childId = firebase.childId,
        title = firebase.title,
        content = firebase.content,
        mediaIds = firebase.mediaIds.joinToString(","),
        date = firebase.date.toDate().time,
        createdAt = firebase.createdAt.toDate().time,
        updatedAt = firebase.updatedAt.toDate().time,
        isSynced = true
    )

    // Child domain model mapping
    fun childEntityToDomain(entity: ChildEntity): Child = Child(
        id = entity.id,
        userId = entity.userId,
        name = entity.name,
        birthDate = Instant.ofEpochMilli(entity.birthDate)
            .atZone(ZoneId.systemDefault())
            .toLocalDate(),
        gender = Gender.valueOf(entity.gender),
        profileImageUrl = entity.profileImageUrl,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt
    )

    fun domainToChildEntity(child: Child): ChildEntity = ChildEntity(
        id = child.id,
        userId = child.userId,
        name = child.name,
        birthDate = child.birthDate.atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli(),
        gender = child.gender.name,
        profileImageUrl = child.profileImageUrl,
        createdAt = child.createdAt,
        updatedAt = child.updatedAt
    )
}