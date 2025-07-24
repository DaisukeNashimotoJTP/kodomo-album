package com.example.kodomo_album.data.firebase.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class FirebaseChild(
    @DocumentId
    val id: String = "",
    
    @PropertyName("userId")
    val userId: String = "",
    
    @PropertyName("name")
    val name: String = "",
    
    @PropertyName("birthDate")
    val birthDate: Timestamp = Timestamp.now(),
    
    @PropertyName("gender")
    val gender: String = "",
    
    @PropertyName("profileImageUrl")
    val profileImageUrl: String? = null,
    
    @PropertyName("createdAt")
    val createdAt: Timestamp = Timestamp.now(),
    
    @PropertyName("updatedAt")
    val updatedAt: Timestamp = Timestamp.now()
)