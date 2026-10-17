package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val fullName: String = "",
    val titleOrProfession: String = "",
    val email: String = "",
    val phone: String = "",
    val summary: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "userId" to userId,
            "fullName" to fullName
        )
        if (titleOrProfession.isNotBlank()) map["titleOrProfession"] = titleOrProfession
        if (email.isNotBlank()) map["email"] = email
        if (phone.isNotBlank()) map["phone"] = phone
        if (summary.isNotBlank()) map["summary"] = summary
        return map
    }
}
