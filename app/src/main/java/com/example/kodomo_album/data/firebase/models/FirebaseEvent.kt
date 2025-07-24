package com.example.kodomo_album.data.firebase.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class FirebaseEvent(
    @DocumentId
    val id: String = "",
    
    @PropertyName("childId")
    val childId: String = "",
    
    @PropertyName("title")
    val title: String = "",
    
    @PropertyName("description")
    val description: String = "",
    
    @PropertyName("eventDate")
    val eventDate: Timestamp = Timestamp.now(),
    
    @PropertyName("mediaIds")
    val mediaIds: List<String> = emptyList(),
    
    @PropertyName("eventType")
    val eventType: String = "",
    
    @PropertyName("createdAt")
    val createdAt: Timestamp = Timestamp.now(),
    
    @PropertyName("updatedAt")
    val updatedAt: Timestamp = Timestamp.now()
)