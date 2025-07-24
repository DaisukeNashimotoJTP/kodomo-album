package com.example.kodomo_album.data.firebase.datasource

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaStorageDataSource @Inject constructor(
    private val storage: FirebaseStorage
) {
    
    companion object {
        private const val MEDIA_FOLDER = "media"
        private const val THUMBNAILS_FOLDER = "thumbnails"
    }
    
    suspend fun uploadImage(childId: String, file: File, fileName: String): String {
        val imageRef = storage.reference
            .child(MEDIA_FOLDER)
            .child(childId)
            .child("images")
            .child(fileName)
        
        return uploadFile(imageRef, file)
    }
    
    suspend fun uploadVideo(childId: String, file: File, fileName: String): String {
        val videoRef = storage.reference
            .child(MEDIA_FOLDER)
            .child(childId)
            .child("videos")
            .child(fileName)
        
        return uploadFile(videoRef, file)
    }
    
    suspend fun uploadThumbnail(childId: String, file: File, fileName: String): String {
        val thumbnailRef = storage.reference
            .child(THUMBNAILS_FOLDER)
            .child(childId)
            .child(fileName)
        
        return uploadFile(thumbnailRef, file)
    }
    
    private suspend fun uploadFile(storageRef: StorageReference, file: File): String {
        val uploadTask = storageRef.putFile(android.net.Uri.fromFile(file))
        uploadTask.await()
        return storageRef.downloadUrl.await().toString()
    }
    
    suspend fun deleteFile(url: String) {
        try {
            val fileRef = storage.getReferenceFromUrl(url)
            fileRef.delete().await()
        } catch (e: Exception) {
            throw e
        }
    }
}