package com.example.kodomo_album.data.firebase.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class FirebaseMilestone(
    @DocumentId
    val id: String = "",
    
    @PropertyName("childId")
    val childId: String = "",
    
    @PropertyName("type")
    val type: String = "",
    
    @PropertyName("title")
    val title: String = "",
    
    @PropertyName("description")
    val description: String = "",
    
    @PropertyName("achievedAt")
    val achievedAt: Timestamp = Timestamp.now(),
    
    @PropertyName("mediaIds")
    val mediaIds: List<String> = emptyList(),
    
    @PropertyName("createdAt")
    val createdAt: Timestamp = Timestamp.now()
)