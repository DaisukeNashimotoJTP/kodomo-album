package com.example.kodomo_album.data.firebase.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class FirebaseMedia(
    @DocumentId
    val id: String = "",
    
    @PropertyName("childId")
    val childId: String = "",
    
    @PropertyName("type")
    val type: String = "",
    
    @PropertyName("url")
    val url: String = "",
    
    @PropertyName("thumbnailUrl")
    val thumbnailUrl: String? = null,
    
    @PropertyName("caption")
    val caption: String? = null,
    
    @PropertyName("takenAt")
    val takenAt: Timestamp = Timestamp.now(),
    
    @PropertyName("uploadedAt")
    val uploadedAt: Timestamp = Timestamp.now()
)