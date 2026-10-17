package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class Certificate(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val organization: String = "",
    val category: String = "Certification",
    val issueDate: String = "",
    val expiryDate: String = "",
    val credentialId: String = "",
    val credentialUrl: String = "",
    val description: String = "",
    val skills: List<String> = emptyList(),
    val gradeOrScore: String = "",
    val attachmentName: String = "",
    val attachmentType: String = "",
    val attachmentPath: String = "",
    val attachmentSize: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "userId" to userId,
            "title" to title,
            "organization" to organization,
            "category" to category,
            "issueDate" to issueDate,
            "skills" to skills
        )
        if (expiryDate.isNotBlank()) map["expiryDate"] = expiryDate
        if (credentialId.isNotBlank()) map["credentialId"] = credentialId
        if (credentialUrl.isNotBlank()) map["credentialUrl"] = credentialUrl
        if (description.isNotBlank()) map["description"] = description
        if (gradeOrScore.isNotBlank()) map["gradeOrScore"] = gradeOrScore
        if (attachmentName.isNotBlank()) map["attachmentName"] = attachmentName
        if (attachmentType.isNotBlank()) map["attachmentType"] = attachmentType
        if (attachmentPath.isNotBlank()) map["attachmentPath"] = attachmentPath
        if (attachmentSize > 0) map["attachmentSize"] = attachmentSize
        return map
    }
}

object CertificateCategories {
    const val ALL = "All"
    const val CERTIFICATION = "Certification"
    const val DEGREE = "Degree"
    const val COURSE = "Course"
    const val AWARD = "Award"
    const val LICENSE = "License"
    const val OTHER = "Other"

    val list = listOf(
        CERTIFICATION,
        DEGREE,
        COURSE,
        AWARD,
        LICENSE,
        OTHER
    )
}
