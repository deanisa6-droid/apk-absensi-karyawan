package com.example.ui.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AppRegistration
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.auth.onGoogleSignInClicked
import com.example.data.model.RegistrationDraft
import com.example.ui.components.GlassBackdrop
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassGoogleButton
import com.example.ui.components.GlassPrimaryButton
import com.example.ui.components.GlassSegmentedTab
import com.example.ui.components.GlassStatusPill
import com.example.ui.components.RegistrationFormFields
import com.example.ui.theme.LocalGlassPalette

@Composable
fun AuthScreen(
    onAuthSuccess: (pendingDraft: RegistrationDraft?, isFromRegistration: Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val palette = LocalGlassPalette.current

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Masuk (Login), 1 = Daftar (Register)
    var registrationDraft by remember { mutableStateOf(RegistrationDraft()) }
    var fieldErrors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var authError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    GlassBackdrop {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Brand Header Emblem
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    palette.cyanGlow.copy(alpha = 0.25f),
                                    palette.violetGlow.copy(alpha = 0.25f)
                                )
                            )
                        )
                        .border(1.5.dp, palette.highlightBorder, RoundedCornerShape(22.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_lumina_foreground),
                        contentDescription = "Logo LuminaID",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(22.dp))
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "LuminaID",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Identitas digital modern berbasis Firebase Auth & Cloud Firestore",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(22.dp))

                // Glass Menu Switcher: Login vs Registration
                GlassSegmentedTab(
                    options = listOf("Menu Login", "Registrasi"),
                    selectedIndex = selectedTab,
                    onSelect = { index ->
                        selectedTab = index
                        authError = null
                        fieldErrors = emptyMap()
                    },
                    testTagPrefix = "auth_menu_tab"
                )

                Spacer(modifier = Modifier.height(18.dp))

                if (authError != null) {
                    GlassCard(
                        shape = RoundedCornerShape(18.dp),
                        contentPadding = PaddingValues(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                            .testTag("auth_error_banner")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ErrorOutline,
                                contentDescription = "Kesalahan",
                                tint = palette.errorCoral
                            )
                            Text(
                                text = authError ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.errorCoral
                            )
                        }
                    }
                }

                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "auth_tab_transition"
                ) { tab ->
                    if (tab == 0) {
                        LoginMenuPanel(
                            isLoading = isLoading,
                            onLoginClick = {
                                authError = null
                                isLoading = true
                                onGoogleSignInClicked(
                                    context = context,
                                    credentialManager = credentialManager,
                                    onAuthSuccess = {
                                        isLoading = false
                                        onAuthSuccess(null, false)
                                    },
                                    onAuthError = { msg ->
                                        isLoading = false
                                        authError = msg
                                    },
                                    scope = scope,
                                    onAuthCancelled = {
                                        isLoading = false
                                        authError = "Proses masuk dibatalkan. Pilih akun Google untuk melanjutkan."
                                    }
                                )
                            },
                            onSwitchToRegister = {
                                authError = null
                                selectedTab = 1
                            }
                        )
                    } else {
                        RegistrationMenuPanel(
                            draft = registrationDraft,
                            errors = fieldErrors,
                            isLoading = isLoading,
                            onDraftChange = { updated ->
                                registrationDraft = updated
                                if (fieldErrors.isNotEmpty()) {
                                    fieldErrors = updated.validate()
                                }
                            },
                            onRegisterClick = {
                                authError = null
                                val validation = registrationDraft.validate()
                                if (validation.isNotEmpty()) {
                                    fieldErrors = validation
                                    authError = "Lengkapi seluruh kolom data diri yang wajib diisi dengan benar."
                                    return@RegistrationMenuPanel
                                }
                                fieldErrors = emptyMap()
                                isLoading = true
                                val preparedDraft = registrationDraft.sanitized()
                                onGoogleSignInClicked(
                                    context = context,
                                    credentialManager = credentialManager,
                                    onAuthSuccess = {
                                        isLoading = false
                                        onAuthSuccess(preparedDraft, true)
                                    },
                                    onAuthError = { msg ->
                                        isLoading = false
                                        authError = msg
                                    },
                                    scope = scope,
                                    onAuthCancelled = {
                                        isLoading = false
                                        authError = "Autentikasi Google dibatalkan. Data diri Anda masih tersimpan di formulir."
                                    }
                                )
                            },
                            onSwitchToLogin = {
                                authError = null
                                selectedTab = 0
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun LoginMenuPanel(
    isLoading: Boolean,
    onLoginClick: () -> Unit,
    onSwitchToRegister: () -> Unit
) {
    val palette = LocalGlassPalette.current
    GlassCard(
        elevated = true,
        accentGlow = true,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassStatusPill(
                text = "Firebase Auth",
                accentColor = palette.cyanGlow,
                icon = Icons.Rounded.VerifiedUser
            )
            GlassStatusPill(
                text = "Sesi Terenkripsi",
                accentColor = palette.verifiedMint,
                icon = Icons.Rounded.Shield
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Selamat Datang Kembali",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Masuk menggunakan akun Google Anda untuk membuka kartu identitas digital dan data profil tersimpan.",
            style = MaterialTheme.typography.bodyMedium,
            color = palette.textSecondary
        )

        Spacer(modifier = Modifier.height(18.dp))

        FeatureHighlightRow(
            icon = Icons.Rounded.Fingerprint,
            title = "Autentikasi Google Satu Ketuk",
            subtitle = "Keamanan standar Google Credential Manager tanpa kata sandi tambahan."
        )
        Spacer(modifier = Modifier.height(12.dp))
        FeatureHighlightRow(
            icon = Icons.Rounded.CloudDone,
            title = "Sinkronisasi Cloud Firestore",
            subtitle = "Data diri dan riwayat aktivitas selalu aman dan tersinkronisasi real-time."
        )

        Spacer(modifier = Modifier.height(24.dp))

        GlassGoogleButton(
            text = "Masuk dengan Google",
            onClick = onLoginClick,
            isLoading = isLoading,
            testTag = "login_google_button"
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onSwitchToRegister)
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Belum mendaftarkan data diri? ",
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textSecondary
            )
            Text(
                text = "Daftar Sekarang",
                style = MaterialTheme.typography.labelLarge,
                color = palette.cyanGlow,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun RegistrationMenuPanel(
    draft: RegistrationDraft,
    errors: Map<String, String>,
    isLoading: Boolean,
    onDraftChange: (RegistrationDraft) -> Unit,
    onRegisterClick: () -> Unit,
    onSwitchToLogin: () -> Unit
) {
    val palette = LocalGlassPalette.current
    GlassCard(
        elevated = true,
        accentGlow = true,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassStatusPill(
                text = "Registrasi Data Diri",
                accentColor = palette.violetGlow,
                icon = Icons.Rounded.AppRegistration
            )
            GlassStatusPill(
                text = "Wajib (*)",
                accentColor = palette.cyanGlow,
                icon = Icons.Rounded.CheckCircle
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Buat Profil Baru",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Lengkapi data diri Anda terlebih dahulu, lalu hubungkan akun Google untuk menyimpan registrasi ke Firebase.",
            style = MaterialTheme.typography.bodyMedium,
            color = palette.textSecondary
        )

        Spacer(modifier = Modifier.height(18.dp))

        RegistrationFormFields(
            draft = draft,
            errors = errors,
            onDraftChange = onDraftChange
        )

        Spacer(modifier = Modifier.height(22.dp))

        GlassPrimaryButton(
            text = "Daftar & Hubungkan Google",
            icon = Icons.Rounded.AppRegistration,
            onClick = onRegisterClick,
            isLoading = isLoading,
            testTag = "register_submit_button"
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onSwitchToLogin)
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sudah memiliki profil? ",
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textSecondary
            )
            Text(
                text = "Masuk di sini",
                style = MaterialTheme.typography.labelLarge,
                color = palette.cyanGlow,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun FeatureHighlightRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    val palette = LocalGlassPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(palette.surfaceLow)
            .border(1.dp, palette.subtleBorder, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(palette.cyanGlow.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = palette.cyanGlow,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textSecondary
            )
        }
    }
}
