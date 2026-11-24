package com.example.data.firebase

import android.content.Context
import com.example.R
import com.example.data.model.ActivityLog
import com.example.data.model.RegistrationDraft
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class UserProfileRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("Pengguna harus masuk dengan Google sebelum mengakses Firestore.")
    }

    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        require(userId.isNotBlank()) { "userId must not be blank" }
        val path = "users/$userId"
        emitAll(
            db.collection("users").document(userId)
                .snapshots()
                .map { snapshot ->
                    if (!snapshot.exists()) {
                        null
                    } else {
                        snapshot.toObject(
                            UserProfile::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    fun observeActivities(userId: String): Flow<List<ActivityLog>> = flow {
        require(userId.isNotBlank()) { "userId must not be blank" }
        val path = "users/$userId/activities"
        emitAll(
            db.collection("users").document(userId).collection("activities")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(20)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        ActivityLog::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun saveRegistrationProfile(
        draft: RegistrationDraft,
        email: String?,
        photoUrl: String?
    ): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}"
        return try {
            val uid = requireUserId()
            val sanitized = draft.sanitized()
            val validationErrors = sanitized.validate()
            require(validationErrors.isEmpty()) {
                validationErrors.values.first()
            }

            val docRef = db.collection("users").document(uid)
            docPath = docRef.path
            val existingSnapshot = docRef.get().await()

            if (!existingSnapshot.exists()) {
                val createPayload = mapOf<String, Any?>(
                    "userId" to uid,
                    "email" to email?.takeIf { it.isNotBlank() }?.take(120),
                    "photoUrl" to photoUrl?.takeIf { it.isNotBlank() }?.take(500),
                    "fullName" to sanitized.fullName,
                    "username" to sanitized.username,
                    "phone" to sanitized.phone,
                    "birthDate" to sanitized.birthDate,
                    "gender" to sanitized.gender,
                    "city" to sanitized.city,
                    "occupation" to sanitized.occupation,
                    "bio" to sanitized.bio,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null } as Map<String, Any>

                docRef.set(createPayload).await()
                appendActivityInternal(
                    uid = uid,
                    action = "REGISTER",
                    title = "Registrasi Data Diri Selesai",
                    description = "Profil @${sanitized.username} berhasil didaftarkan ke Cloud Firestore."
                )
            } else {
                val updatePayload = mapOf<String, Any?>(
                    "email" to email?.takeIf { it.isNotBlank() }?.take(120),
                    "photoUrl" to photoUrl?.takeIf { it.isNotBlank() }?.take(500),
                    "fullName" to sanitized.fullName,
                    "username" to sanitized.username,
                    "phone" to sanitized.phone,
                    "birthDate" to sanitized.birthDate,
                    "gender" to sanitized.gender,
                    "city" to sanitized.city,
                    "occupation" to sanitized.occupation,
                    "bio" to sanitized.bio,
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null } as Map<String, Any>

                docRef.update(updatePayload).await()
                appendActivityInternal(
                    uid = uid,
                    action = "UPDATE_PROFILE",
                    title = "Data Diri Diperbarui",
                    description = "Informasi profil pribadi untuk @${sanitized.username} disinkronkan."
                )
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateProfileFields(draft: RegistrationDraft): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}"
        return try {
            val uid = requireUserId()
            val sanitized = draft.sanitized()
            val validationErrors = sanitized.validate()
            require(validationErrors.isEmpty()) {
                validationErrors.values.first()
            }

            val docRef = db.collection("users").document(uid)
            docPath = docRef.path
            val updatePayload = mapOf<String, Any>(
                "fullName" to sanitized.fullName,
                "username" to sanitized.username,
                "phone" to sanitized.phone,
                "birthDate" to sanitized.birthDate,
                "gender" to sanitized.gender,
                "city" to sanitized.city,
                "occupation" to sanitized.occupation,
                "bio" to sanitized.bio,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.update(updatePayload).await()
            appendActivityInternal(
                uid = uid,
                action = "UPDATE_PROFILE",
                title = "Pembaruan Profil",
                description = "Data diri @${sanitized.username} di ${sanitized.city} telah diperbarui."
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun logSignInEvent(): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/activities"
        return try {
            val uid = requireUserId()
            docPath = "users/$uid/activities"
            appendActivityInternal(
                uid = uid,
                action = "LOGIN",
                title = "Masuk via Google Sign-In",
                description = "Sesi terautentikasi secara aman dengan Firebase Authentication."
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteUserProfile(): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}"
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid)
            docPath = docRef.path
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    private suspend fun appendActivityInternal(
        uid: String,
        action: String,
        title: String,
        description: String
    ) {
        val actRef = db.collection("users").document(uid).collection("activities").document()
        val payload = mapOf(
            "userId" to uid,
            "action" to action,
            "title" to title.take(80),
            "description" to description.take(160),
            "createdAt" to FieldValue.serverTimestamp()
        )
        actRef.set(payload).await()
    }
}
