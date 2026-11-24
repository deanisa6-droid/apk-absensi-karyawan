package com.example.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AppRegistration
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.RegistrationDraft
import com.example.ui.components.GlassBackdrop
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassPrimaryButton
import com.example.ui.components.GlassStatusPill
import com.example.ui.components.RegistrationFormFields
import com.example.ui.theme.LocalGlassPalette
import com.google.firebase.auth.FirebaseUser

@Composable
fun CompleteRegistrationScreen(
    firebaseUser: FirebaseUser,
    isSaving: Boolean,
    errorMessage: String?,
    onSubmitProfile: (RegistrationDraft) -> Unit,
    onSignOutClick: () -> Unit
) {
    val palette = LocalGlassPalette.current
    val initialName = firebaseUser.displayName.orEmpty()
    val suggestedUsername = firebaseUser.email
        ?.substringBefore("@")
        ?.replace(Regex("[^a-zA-Z0-9_.]"), "")
        ?.take(20)
        .orEmpty()

    var draft by remember(firebaseUser.uid) {
        mutableStateOf(
            RegistrationDraft(
                fullName = initialName,
                username = suggestedUsername
            )
        )
    }
    var fieldErrors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var localError by remember { mutableStateOf<String?>(null) }

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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Connected Google Account Banner
                GlassCard(
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (firebaseUser.photoUrl != null) {
                                AsyncImage(
                                    model = firebaseUser.photoUrl.toString(),
                                    contentDescription = "Foto profil Google",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .border(1.5.dp, palette.cyanGlow, CircleShape)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(palette.cyanGlow.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Person,
                                        contentDescription = "Avatar",
                                        tint = palette.cyanGlow
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = firebaseUser.displayName ?: "Akun Google",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = firebaseUser.email ?: firebaseUser.uid,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = palette.textSecondary
                                )
                            }
                        }

                        TextButton(
                            onClick = onSignOutClick,
                            modifier = Modifier.testTag("complete_reg_signout_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Logout,
                                contentDescription = "Ganti Akun",
                                tint = palette.errorCoral,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Ganti",
                                color = palette.errorCoral,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                val activeError = localError ?: errorMessage
                if (activeError != null) {
                    GlassCard(
                        shape = RoundedCornerShape(18.dp),
                        contentPadding = PaddingValues(14.dp),
                        modifier = Modifier.fillMaxWidth()
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
                                text = activeError,
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.errorCoral
                            )
                        }
                    }
                }

                GlassCard(
                    elevated = true,
                    accentGlow = true,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GlassStatusPill(
                        text = "Langkah Terakhir",
                        accentColor = palette.cyanGlow,
                        icon = Icons.Rounded.VerifiedUser
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Lengkapi Data Diri Anda",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Akun Google Anda belum terdaftar di LuminaID. Lengkapi data diri di bawah untuk menyelesaikan pendaftaran.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textSecondary
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    RegistrationFormFields(
                        draft = draft,
                        errors = fieldErrors,
                        onDraftChange = { updated ->
                            draft = updated
                            if (fieldErrors.isNotEmpty()) {
                                fieldErrors = updated.validate()
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    GlassPrimaryButton(
                        text = "Simpan & Selesaikan Registrasi",
                        icon = Icons.Rounded.AppRegistration,
                        isLoading = isSaving,
                        onClick = {
                            localError = null
                            val validation = draft.validate()
                            if (validation.isNotEmpty()) {
                                fieldErrors = validation
                                localError = "Periksa kembali kolom data diri yang bertanda merah."
                            } else {
                                fieldErrors = emptyMap()
                                onSubmitProfile(draft.sanitized())
                            }
                        },
                        testTag = "complete_registration_submit"
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
