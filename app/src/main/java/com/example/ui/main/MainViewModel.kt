package com.example.ui.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.UserProfileRepository
import com.example.data.model.ActivityLog
import com.example.data.model.RegistrationDraft
import com.example.data.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class MainViewModel(
    private val repository: UserProfileRepository,
    private val currentUserId: String
) : ViewModel() {

    val profileState: StateFlow<UiState<UserProfile?>> = repository.observeUserProfile(currentUserId)
        .map<UserProfile?, UiState<UserProfile?>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing user profile", rootError)
            emit(UiState.Error(rootError.localizedMessage ?: "Gagal memuat profil dari Firestore."))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val activitiesState: StateFlow<UiState<List<ActivityLog>>> = repository.observeActivities(currentUserId)
        .map<List<ActivityLog>, UiState<List<ActivityLog>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing user activities", rootError)
            emit(UiState.Error(rootError.localizedMessage ?: "Gagal memuat riwayat aktivitas."))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    private val _actionError = MutableStateFlow<String?>(null)
    val actionError: StateFlow<String?> = _actionError.asStateFlow()

    private var hasHandledPendingDraft = false
    private var hasLoggedSessionSignIn = false

    fun applyPendingRegistrationIfNeeded(
        pendingDraft: RegistrationDraft?,
        email: String?,
        photoUrl: String?,
        onConsumed: () -> Unit
    ) {
        if (pendingDraft == null || hasHandledPendingDraft) return
        hasHandledPendingDraft = true
        hasLoggedSessionSignIn = true
        viewModelScope.launch {
            _isSaving.value = true
            _actionError.value = null
            val result = repository.saveRegistrationProfile(pendingDraft, email, photoUrl)
            _isSaving.value = false
            result.fold(
                onSuccess = {
                    _actionMessage.value = "Selamat datang! Data diri Anda berhasil didaftarkan."
                    onConsumed()
                },
                onFailure = { err ->
                    _actionError.value = err.localizedMessage ?: "Gagal menyimpan data registrasi."
                }
            )
        }
    }

    fun recordLoginIfInteractive(isInteractiveLogin: Boolean, onConsumed: () -> Unit) {
        if (!isInteractiveLogin || hasLoggedSessionSignIn) return
        hasLoggedSessionSignIn = true
        viewModelScope.launch {
            repository.logSignInEvent()
            onConsumed()
        }
    }

    fun registerProfile(
        draft: RegistrationDraft,
        email: String?,
        photoUrl: String?,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            _actionError.value = null
            val result = repository.saveRegistrationProfile(draft, email, photoUrl)
            _isSaving.value = false
            result.fold(
                onSuccess = {
                    _actionMessage.value = "Registrasi data diri berhasil disimpan."
                    onSuccess()
                },
                onFailure = { err ->
                    _actionError.value = err.localizedMessage ?: "Gagal menyimpan data diri."
                }
            )
        }
    }

    fun updateProfile(
        draft: RegistrationDraft,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            _actionError.value = null
            val result = repository.updateProfileFields(draft)
            _isSaving.value = false
            result.fold(
                onSuccess = {
                    _actionMessage.value = "Perubahan data diri berhasil disimpan."
                    onSuccess()
                },
                onFailure = { err ->
                    _actionError.value = err.localizedMessage ?: "Gagal memperbarui data diri."
                }
            )
        }
    }

    fun deleteProfile(onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            _isSaving.value = true
            _actionError.value = null
            val result = repository.deleteUserProfile()
            _isSaving.value = false
            result.fold(
                onSuccess = {
                    _actionMessage.value = "Profil data diri telah dihapus."
                    onDeleted()
                },
                onFailure = { err ->
                    _actionError.value = err.localizedMessage ?: "Gagal menghapus profil."
                }
            )
        }
    }

    fun clearFeedback() {
        _actionMessage.value = null
        _actionError.value = null
    }

    private companion object {
        const val TAG = "MainViewModel"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
