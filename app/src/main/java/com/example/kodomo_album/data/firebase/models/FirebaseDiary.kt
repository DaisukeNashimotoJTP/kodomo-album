package com.example.kodomo_album.data.firebase.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class FirebaseDiary(
    @DocumentId
    val id: String = "",
    
    @PropertyName("childId")
    val childId: String = "",
    
    @PropertyName("title")
    val title: String = "",
    
    @PropertyName("content")
    val content: String = "",
    
    @PropertyName("mediaIds")
    val mediaIds: List<String> = emptyList(),
    
    @PropertyName("date")
    val date: Timestamp = Timestamp.now(),
    
    @PropertyName("createdAt")
    val createdAt: Timestamp = Timestamp.now(),
    
    @PropertyName("updatedAt")
    val updatedAt: Timestamp = Timestamp.now()
)