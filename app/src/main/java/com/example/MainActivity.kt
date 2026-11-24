package com.example

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.auth.attemptAutoSignIn
import com.example.auth.authStateFlow
import com.example.auth.signOut
import com.example.data.firebase.UserProfileRepository
import com.example.data.model.RegistrationDraft
import com.example.ui.auth.AuthScreen
import com.example.ui.components.GlassBackdrop
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassPrimaryButton
import com.example.ui.main.CompleteRegistrationScreen
import com.example.ui.main.DashboardScreen
import com.example.ui.main.MainViewModel
import com.example.ui.main.UiState
import com.example.ui.theme.LocalGlassPalette
import com.example.ui.theme.LuminaTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setContent {
            LuminaTheme {
                AppContent()
            }
        }
    }
}

@Composable
fun AppContent(auth: FirebaseAuth = Firebase.auth) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    var pendingRegistrationDraft by remember { mutableStateOf<RegistrationDraft?>(null) }
    var isInteractiveSignIn by remember { mutableStateOf(false) }
    var hasAttemptedAutoSignIn by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!hasAttemptedAutoSignIn && auth.currentUser == null) {
            hasAttemptedAutoSignIn = true
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = {},
                onUnauthenticated = {},
                scope = scope
            )
        }
    }

    val user = currentUser
    if (user == null) {
        AuthScreen(
            onAuthSuccess = { draft, fromRegistration ->
                pendingRegistrationDraft = draft
                isInteractiveSignIn = !fromRegistration
            }
        )
    } else {
        key(user.uid) {
            AuthenticatedSession(
                firebaseUser = user,
                pendingDraft = pendingRegistrationDraft,
                isInteractiveSignIn = isInteractiveSignIn,
                onPendingDraftConsumed = { pendingRegistrationDraft = null },
                onInteractiveSignInConsumed = { isInteractiveSignIn = false },
                onSignOutClick = {
                    pendingRegistrationDraft = null
                    isInteractiveSignIn = false
                    signOut(credentialManager = credentialManager, scope = scope)
                }
            )
        }
    }
}

@Composable
private fun AuthenticatedSession(
    firebaseUser: FirebaseUser,
    pendingDraft: RegistrationDraft?,
    isInteractiveSignIn: Boolean,
    onPendingDraftConsumed: () -> Unit,
    onInteractiveSignInConsumed: () -> Unit,
    onSignOutClick: () -> Unit
) {
    val currentUserId = firebaseUser.uid
    val viewModel: MainViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                MainViewModel(UserProfileRepository(db), currentUserId)
            }
        }
    )

    val profileState by viewModel.profileState.collectAsStateWithLifecycle()
    val activitiesState by viewModel.activitiesState.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val actionMessage by viewModel.actionMessage.collectAsStateWithLifecycle()
    val actionError by viewModel.actionError.collectAsStateWithLifecycle()

    LaunchedEffect(currentUserId, pendingDraft) {
        if (pendingDraft != null) {
            viewModel.applyPendingRegistrationIfNeeded(
                pendingDraft = pendingDraft,
                email = firebaseUser.email,
                photoUrl = firebaseUser.photoUrl?.toString(),
                onConsumed = onPendingDraftConsumed
            )
        }
    }

    LaunchedEffect(profileState, isInteractiveSignIn) {
        val state = profileState
        if (state is UiState.Success && state.data != null && isInteractiveSignIn) {
            viewModel.recordLoginIfInteractive(
                isInteractiveLogin = true,
                onConsumed = onInteractiveSignInConsumed
            )
        }
    }

    when (val state = profileState) {
        is UiState.Loading -> {
            LoadingSessionScreen(isSavingProfile = isSaving || pendingDraft != null)
        }

        is UiState.Error -> {
            ErrorSessionScreen(
                message = state.message,
                onSignOutClick = onSignOutClick
            )
        }

        is UiState.Success -> {
            val profile = state.data
            if (profile == null) {
                if (pendingDraft != null && isSaving) {
                    LoadingSessionScreen(isSavingProfile = true)
                } else {
                    CompleteRegistrationScreen(
                        firebaseUser = firebaseUser,
                        isSaving = isSaving,
                        errorMessage = actionError,
                        onSubmitProfile = { draft ->
                            viewModel.registerProfile(
                                draft = draft,
                                email = firebaseUser.email,
                                photoUrl = firebaseUser.photoUrl?.toString()
                            )
                        },
                        onSignOutClick = onSignOutClick
                    )
                }
            } else {
                DashboardScreen(
                    firebaseUser = firebaseUser,
                    profile = profile,
                    activitiesState = activitiesState,
                    isSaving = isSaving,
                    actionMessage = actionMessage,
                    actionError = actionError,
                    onClearFeedback = { viewModel.clearFeedback() },
                    onUpdateProfile = { draft, onSuccess ->
                        viewModel.updateProfile(draft, onSuccess)
                    },
                    onDeleteProfile = { viewModel.deleteProfile() },
                    onSignOutClick = onSignOutClick
                )
            }
        }
    }
}

@Composable
private fun LoadingSessionScreen(isSavingProfile: Boolean) {
    val palette = LocalGlassPalette.current
    GlassBackdrop {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            GlassCard(
                elevated = true,
                accentGlow = true,
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = palette.cyanGlow,
                        modifier = Modifier.size(40.dp),
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isSavingProfile) "Menyimpan data diri ke Firebase..."
                        else "Menghubungkan sesi LuminaID...",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorSessionScreen(
    message: String,
    onSignOutClick: () -> Unit
) {
    val palette = LocalGlassPalette.current
    GlassBackdrop {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            GlassCard(elevated = true) {
                Text(
                    text = "Koneksi Profil Terkendala",
                    style = MaterialTheme.typography.headlineSmall,
                    color = palette.errorCoral
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textSecondary
                )
                Spacer(modifier = Modifier.height(20.dp))
                GlassPrimaryButton(
                    text = "Kembali ke Menu Login",
                    onClick = onSignOutClick,
                    testTag = "error_session_signout"
                )
            }
        }
    }
}
