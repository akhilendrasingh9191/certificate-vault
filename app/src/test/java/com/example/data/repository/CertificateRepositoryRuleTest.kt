package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.Certificate
import com.example.data.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class CertificateRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createCertificate_validPayload_createsDocumentAndReturnsId() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = CertificateRepository(firestore)

        val sampleCert = Certificate(
            title = "AWS Certified Solutions Architect - Associate",
            organization = "Amazon Web Services",
            category = "Certification",
            issueDate = "2024-05-10",
            description = "Architecting resilient distributed systems on AWS",
            skills = listOf("Cloud", "AWS", "Architecture")
        )

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createCertificate(sampleCert)
        }
        assertTrue(createResult.isSuccess)
        val certId = createResult.getOrThrow()
        assertTrue(certId.isNotEmpty())
    }

    @Test
    fun observeCertificates_authenticatedOwner_emitsRealtimeUpdates() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = CertificateRepository(firestore)

        val sampleCert = Certificate(
            title = "Machine Learning Specialization",
            organization = "DeepLearning.AI / Coursera",
            category = "Course",
            issueDate = "2024-06-01",
            description = "Supervised and unsupervised learning, deep learning foundations"
        )
        val certId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createCertificate(sampleCert).getOrThrow()
        }

        val emittedList = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeCertificates().first { list ->
                list.any { it.id == certId }
            }
        }
        assertTrue(emittedList.any { it.id == certId })
    }

    @Test
    fun observeCertificates_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = CertificateRepository(firestore)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeCertificates().first()
            }
            fail("Expected exception when unauthenticated")
        } catch (e: Exception) {
            // Unauthenticated callers fail fast or with Firestore exception
            assertTrue(e is IllegalStateException || e is FirebaseFirestoreException)
        }
    }

    @Test
    fun saveAndObserveUserProfile_authenticatedOwner_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = CertificateRepository(firestore)

        val profile = UserProfile(
            userId = uid,
            fullName = "Alice Developer",
            titleOrProfession = "Staff Software Engineer",
            email = ALICE_EMAIL,
            summary = "Passionate engineer with extensive cloud and system design experience."
        )

        val saveResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveUserProfile(profile)
        }
        assertTrue(saveResult.isSuccess)

        val fetchedProfile = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeUserProfile().first { it != null }
        }
        assertNotNull(fetchedProfile)
        assertEquals("Alice Developer", fetchedProfile?.fullName)
    }

    private companion object {
        const val ALICE_EMAIL = "alice_cert@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
