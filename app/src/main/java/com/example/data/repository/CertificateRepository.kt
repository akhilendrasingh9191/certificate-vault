package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.Certificate
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class CertificateRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(resolveFirestore(context))

    companion object {
        fun resolveFirestore(context: Context): FirebaseFirestore {
            val resId = context.applicationContext.resources.getIdentifier(
                "firestore_database_id",
                "string",
                context.packageName
            )
            val dbId = if (resId != 0) context.applicationContext.getString(resId) else null
            return if (!dbId.isNullOrBlank() && dbId != "(default)") {
                try {
                    FirebaseFirestore.getInstance(dbId)
                } catch (e: Exception) {
                    FirebaseFirestore.getInstance()
                }
            } else {
                FirebaseFirestore.getInstance()
            }
        }
    }

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in before accessing Firestore.")
    }

    fun observeCertificates(): Flow<List<Certificate>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/certificates"
        emitAll(
            db.collection("users").document(uid).collection("certificates")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(Certificate::class.java).sortedByDescending { it.issueDate }
                }
                .catch { error ->
                    if (error is Exception) {
                        handleFirestoreError(error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun createCertificate(certificate: Certificate): Result<String> {
        return try {
            val uid = requireUserId()
            val certsRef = db.collection("users").document(uid).collection("certificates")
            val payload = certificate.toMap() + mapOf(
                "userId" to uid,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            val docRef = certsRef.add(payload).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            val uid = auth.currentUser?.uid ?: "unknown"
            handleFirestoreError(e, OperationType.CREATE, "users/$uid/certificates")
            Result.failure(e)
        }
    }

    suspend fun updateCertificate(certificate: Certificate): Result<Unit> {
        return try {
            val uid = requireUserId()
            require(certificate.id.isNotBlank()) { "Certificate ID must not be blank for update" }
            val docRef = db.collection("users").document(uid).collection("certificates").document(certificate.id)
            val payload = certificate.toMap() + mapOf(
                "userId" to uid,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.update(payload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            val uid = auth.currentUser?.uid ?: "unknown"
            handleFirestoreError(e, OperationType.UPDATE, "users/$uid/certificates/${certificate.id}")
            Result.failure(e)
        }
    }

    suspend fun deleteCertificate(certificateId: String): Result<Unit> {
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid).collection("certificates").document(certificateId)
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            val uid = auth.currentUser?.uid ?: "unknown"
            handleFirestoreError(e, OperationType.DELETE, "users/$uid/certificates/$certificateId")
            Result.failure(e)
        }
    }

    fun observeUserProfile(): Flow<UserProfile?> = flow {
        val uid = requireUserId()
        val path = "users/$uid"
        emitAll(
            db.collection("users").document(uid)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(UserProfile::class.java)
                    } else {
                        null
                    }
                }
                .catch { error ->
                    if (error is Exception) {
                        handleFirestoreError(error, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid)
            val payload = profile.toMap() + mapOf(
                "userId" to uid,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            val snapshot = docRef.get().await()
            if (!snapshot.exists()) {
                val createPayload = payload + mapOf("createdAt" to FieldValue.serverTimestamp())
                docRef.set(createPayload).await()
            } else {
                docRef.update(payload).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            val uid = auth.currentUser?.uid ?: "unknown"
            handleFirestoreError(e, OperationType.WRITE, "users/$uid")
            Result.failure(e)
        }
    }
}
