package com.example.kodomoalbum.data.local.converter

import androidx.room.TypeConverter
import com.example.kodomoalbum.domain.model.FamilyMember
import com.example.kodomoalbum.domain.model.FamilyRole
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDateTime

class FamilyMemberConverter {
    
    @TypeConverter
    fun fromFamilyMemberList(members: List<FamilyMember>): String {
        return Gson().toJson(members)
    }
    
    @TypeConverter
    fun toFamilyMemberList(membersString: String): List<FamilyMember> {
        val listType = object : TypeToken<List<FamilyMemberDto>>() {}.type
        val memberDtos: List<FamilyMemberDto> = Gson().fromJson(membersString, listType)
        return memberDtos.map { dto ->
            FamilyMember(
                userId = dto.userId,
                email = dto.email,
                name = dto.name,
                role = FamilyRole.valueOf(dto.role),
                joinedAt = LocalDateTime.parse(dto.joinedAt)
            )
        }
    }
    
    private data class FamilyMemberDto(
        val userId: String,
        val email: String,
        val name: String,
        val role: String,
        val joinedAt: String
    )
}