package com.example.kodomo_album.data.mapper

import com.example.kodomo_album.data.firebase.models.*
import com.example.kodomo_album.data.local.entity.*
import com.example.kodomo_album.domain.model.Child
import com.example.kodomo_album.domain.model.Diary
import com.example.kodomo_album.domain.model.Gender
import com.example.kodomo_album.domain.model.GrowthRecord
import com.example.kodomo_album.domain.model.Media
import com.example.kodomo_album.domain.model.MediaType
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

    // Diary domain model mapping
    fun diaryEntityToDomain(entity: DiaryEntity): Diary = Diary(
        id = entity.id,
        childId = entity.childId,
        title = entity.title,
        content = entity.content,
        mediaIds = if (entity.mediaIds.isEmpty()) emptyList() else entity.mediaIds.split(","),
        date = Instant.ofEpochMilli(entity.date)
            .atZone(ZoneId.systemDefault())
            .toLocalDate(),
        createdAt = Instant.ofEpochMilli(entity.createdAt)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime(),
        updatedAt = Instant.ofEpochMilli(entity.updatedAt)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
    )

    fun diaryToEntity(diary: Diary): DiaryEntity = DiaryEntity(
        id = diary.id,
        childId = diary.childId,
        title = diary.title,
        content = diary.content,
        mediaIds = diary.mediaIds.joinToString(","),
        date = diary.date.atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli(),
        createdAt = diary.createdAt.atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli(),
        updatedAt = diary.updatedAt.atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli(),
        isSynced = false
    )

    fun diaryToFirebase(diary: Diary): FirebaseDiary = FirebaseDiary(
        id = diary.id,
        childId = diary.childId,
        title = diary.title,
        content = diary.content,
        mediaIds = diary.mediaIds,
        date = Timestamp(diary.date.atStartOfDay(ZoneId.systemDefault()).toInstant()),
        createdAt = Timestamp(diary.createdAt.atZone(ZoneId.systemDefault()).toInstant()),
        updatedAt = Timestamp(diary.updatedAt.atZone(ZoneId.systemDefault()).toInstant())
    )

    // GrowthRecord mapping
    fun domainToFirebaseGrowthRecord(growth: GrowthRecord): FirebaseGrowthRecord = FirebaseGrowthRecord(
        id = growth.id,
        childId = growth.childId,
        height = growth.height,
        weight = growth.weight,
        headCircumference = growth.headCircumference,
        recordedAt = Timestamp(growth.recordedAt.atStartOfDay(ZoneId.systemDefault()).toInstant()),
        notes = growth.notes,
        createdAt = Timestamp.now()
    )

    fun firebaseGrowthRecordToDomain(firebase: FirebaseGrowthRecord): GrowthRecord = GrowthRecord(
        id = firebase.id,
        childId = firebase.childId,
        height = firebase.height,
        weight = firebase.weight,
        headCircumference = firebase.headCircumference,
        recordedAt = firebase.recordedAt.toDate().toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate(),
        notes = firebase.notes
    )
}

// Extension functions for easier usage
fun MediaEntity.toDomain(): Media = Media(
    id = id,
    childId = childId,
    type = MediaType.valueOf(type),
    url = url,
    thumbnailUrl = thumbnailUrl,
    caption = caption,
    takenAt = Instant.ofEpochMilli(takenAt)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime(),
    uploadedAt = Instant.ofEpochMilli(uploadedAt)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
)

fun Media.toEntity(): MediaEntity = MediaEntity(
    id = id,
    childId = childId,
    type = type.name,
    url = url,
    thumbnailUrl = thumbnailUrl,
    caption = caption,
    takenAt = takenAt.atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli(),
    uploadedAt = uploadedAt.atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli(),
    isUploaded = true,
    localPath = null
)

fun Media.toFirebaseMedia(): FirebaseMedia = FirebaseMedia(
    id = id,
    childId = childId,
    type = type.name,
    url = url,
    thumbnailUrl = thumbnailUrl,
    caption = caption,
    takenAt = Timestamp(takenAt.atZone(ZoneId.systemDefault()).toInstant()),
    uploadedAt = Timestamp(uploadedAt.atZone(ZoneId.systemDefault()).toInstant())
)

fun FirebaseMedia.toDomain(): Media = Media(
    id = id,
    childId = childId,
    type = MediaType.valueOf(type),
    url = url,
    thumbnailUrl = thumbnailUrl,
    caption = caption,
    takenAt = takenAt.toDate().toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime(),
    uploadedAt = uploadedAt.toDate().toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
)