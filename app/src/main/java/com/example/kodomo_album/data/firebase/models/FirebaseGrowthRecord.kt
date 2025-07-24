package com.example.kodomo_album.data.firebase.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class FirebaseGrowthRecord(
    @DocumentId
    val id: String = "",
    
    @PropertyName("childId")
    val childId: String = "",
    
    @PropertyName("height")
    val height: Double? = null,
    
    @PropertyName("weight")
    val weight: Double? = null,
    
    @PropertyName("headCircumference")
    val headCircumference: Double? = null,
    
    @PropertyName("recordedAt")
    val recordedAt: Timestamp = Timestamp.now(),
    
    @PropertyName("notes")
    val notes: String? = null,
    
    @PropertyName("createdAt")
    val createdAt: Timestamp = Timestamp.now()
)