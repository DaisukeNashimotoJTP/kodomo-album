package com.example.kodomo_album.data.repository

import com.example.kodomo_album.core.util.Resource
import com.example.kodomo_album.data.firebase.datasource.MediaFirestoreDataSource
import com.example.kodomo_album.data.firebase.datasource.MediaStorageDataSource
import com.example.kodomo_album.data.firebase.models.FirebaseMedia
import com.example.kodomo_album.data.local.dao.MediaDao
import com.example.kodomo_album.data.local.entity.MediaEntity
import com.example.kodomo_album.domain.model.MediaMetadata
import com.example.kodomo_album.domain.model.MediaType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.io.File
import java.time.LocalDateTime

class MediaRepositoryImplTest {

    private lateinit var mediaDao: MediaDao
    private lateinit var mediaFirestoreDataSource: MediaFirestoreDataSource
    private lateinit var mediaStorageDataSource: MediaStorageDataSource
    private lateinit var mediaRepository: MediaRepositoryImpl

    @Before
    fun setUp() {
        mediaDao = mockk(relaxed = true)
        mediaFirestoreDataSource = mockk(relaxed = true)
        mediaStorageDataSource = mockk(relaxed = true)
        mediaRepository = MediaRepositoryImpl(
            mediaDao,
            mediaFirestoreDataSource,
            mediaStorageDataSource
        )
    }

    @Test
    fun `uploadPhoto should return success when all operations succeed`() = runTest {
        // Given
        val file = mockk<File>()
        val metadata = MediaMetadata(
            childId = "child1",
            caption = "Test photo",
            type = MediaType.PHOTO
        )
        val imageUrl = "https://example.com/photo.jpg"

        coEvery { mediaStorageDataSource.uploadImage(any(), any(), any()) } returns imageUrl
        coEvery { mediaFirestoreDataSource.createMedia(any()) } returns Result.success(mockk())

        // When
        val result = mediaRepository.uploadPhoto(file, metadata)

        // Then
        assertTrue(result is Resource.Success)
        val media = (result as Resource.Success).data!!
        assertEquals("child1", media.childId)
        assertEquals(MediaType.PHOTO, media.type)
        assertEquals(imageUrl, media.url)
        assertEquals("Test photo", media.caption)

        coVerify { mediaStorageDataSource.uploadImage("child1", file, any()) }
        coVerify { mediaDao.insertMedia(any()) }
        coVerify { mediaFirestoreDataSource.createMedia(any()) }
    }

    @Test
    fun `uploadVideo should return success when all operations succeed`() = runTest {
        // Given
        val file = mockk<File>()
        val metadata = MediaMetadata(
            childId = "child1",
            caption = "Test video",
            type = MediaType.VIDEO
        )
        val videoUrl = "https://example.com/video.mp4"

        coEvery { mediaStorageDataSource.uploadVideo(any(), any(), any()) } returns videoUrl
        coEvery { mediaFirestoreDataSource.createMedia(any()) } returns Result.success(mockk())

        // When
        val result = mediaRepository.uploadVideo(file, metadata)

        // Then
        assertTrue(result is Resource.Success)
        val media = (result as Resource.Success).data!!
        assertEquals("child1", media.childId)
        assertEquals(MediaType.VIDEO, media.type)
        assertEquals(videoUrl, media.url)
        assertEquals("Test video", media.caption)

        coVerify { mediaStorageDataSource.uploadVideo("child1", file, any()) }
        coVerify { mediaDao.insertMedia(any()) }
        coVerify { mediaFirestoreDataSource.createMedia(any()) }
    }

    @Test
    fun `getMediaList should return flow of media list`() = runTest {
        // Given
        val childId = "child1"
        val mediaEntities = listOf(
            MediaEntity(
                id = "media1",
                childId = childId,
                type = "PHOTO",
                url = "https://example.com/photo.jpg",
                thumbnailUrl = "https://example.com/thumb.jpg",
                caption = "Test photo",
                takenAt = System.currentTimeMillis(),
                uploadedAt = System.currentTimeMillis(),
                isUploaded = true,
                localPath = null
            )
        )

        coEvery { mediaDao.getMediaByChildId(childId) } returns flowOf(mediaEntities)

        // When
        val result = mediaRepository.getMediaList(childId)

        // Then
        result.collect { mediaList ->
            assertEquals(1, mediaList.size)
            assertEquals("media1", mediaList[0].id)
            assertEquals(childId, mediaList[0].childId)
            assertEquals(MediaType.PHOTO, mediaList[0].type)
        }
    }

    @Test
    fun `deleteMedia should delete from storage, dao and firestore`() = runTest {
        // Given
        val mediaId = "media1"
        val mediaEntity = MediaEntity(
            id = mediaId,
            childId = "child1",
            type = "PHOTO",
            url = "https://example.com/photo.jpg",
            thumbnailUrl = "https://example.com/thumb.jpg",
            caption = "Test photo",
            takenAt = System.currentTimeMillis(),
            uploadedAt = System.currentTimeMillis(),
            isUploaded = true,
            localPath = null
        )

        coEvery { mediaDao.getMediaById(mediaId) } returns mediaEntity
        coEvery { mediaFirestoreDataSource.deleteMedia(mediaId) } returns Result.success(Unit)

        // When
        val result = mediaRepository.deleteMedia(mediaId)

        // Then
        assertTrue(result is Resource.Success)
        coVerify { mediaStorageDataSource.deleteFile("https://example.com/photo.jpg") }
        coVerify { mediaStorageDataSource.deleteFile("https://example.com/thumb.jpg") }
        coVerify { mediaDao.deleteMedia(mediaId) }
        coVerify { mediaFirestoreDataSource.deleteMedia(mediaId) }
    }

    @Test
    fun `uploadPhoto should return error when storage upload fails`() = runTest {
        // Given
        val file = mockk<File>()
        val metadata = MediaMetadata(
            childId = "child1",
            type = MediaType.PHOTO
        )

        coEvery { mediaStorageDataSource.uploadImage(any(), any(), any()) } throws Exception("Upload failed")

        // When
        val result = mediaRepository.uploadPhoto(file, metadata)

        // Then
        assertTrue(result is Resource.Error)
        assertTrue((result as Resource.Error).message?.contains("Upload failed") == true ||
                  result.message?.contains("予期しないエラーが発生しました") == true)
    }
}