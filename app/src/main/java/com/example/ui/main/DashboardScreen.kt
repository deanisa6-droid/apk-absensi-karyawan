package com.example.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.HowToReg
import androidx.compose.material.icons.rounded.LocationCity
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.Wc
import androidx.compose.material.icons.rounded.WorkOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.ActivityLog
import com.example.data.model.RegistrationDraft
import com.example.data.model.UserProfile
import com.example.ui.components.GlassBackdrop
import com.example.ui.components.GlassBottomNavBar
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassPrimaryButton
import com.example.ui.components.GlassStatusPill
import com.example.ui.components.NavTabItem
import com.example.ui.components.RegistrationFormFields
import com.example.ui.theme.LocalGlassPalette
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun DashboardScreen(
    firebaseUser: FirebaseUser,
    profile: UserProfile,
    activitiesState: UiState<List<ActivityLog>>,
    isSaving: Boolean,
    actionMessage: String?,
    actionError: String?,
    onClearFeedback: () -> Unit,
    onUpdateProfile: (RegistrationDraft, onSuccess: () -> Unit) -> Unit,
    onDeleteProfile: () -> Unit,
    onSignOutClick: () -> Unit
) {
    val palette = LocalGlassPalette.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var isEditingProfile by remember { mutableStateOf(false) }

    // Support back button returning from Edit mode or secondary tabs to Beranda
    BackHandler(enabled = isEditingProfile || selectedTab != 0) {
        if (isEditingProfile) {
            isEditingProfile = false
        } else {
            selectedTab = 0
        }
    }

    val navItems = remember {
        listOf(
            NavTabItem("Beranda", Icons.Outlined.Home, Icons.Rounded.Home),
            NavTabItem("Data Diri", Icons.Outlined.Badge, Icons.Rounded.Badge),
            NavTabItem("Akun", Icons.Outlined.Security, Icons.Rounded.Security)
        )
    }

    GlassBackdrop {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                GlassBottomNavBar(
                    items = navItems,
                    selectedIndex = selectedTab,
                    onSelect = { tab ->
                        isEditingProfile = false
                        onClearFeedback()
                        selectedTab = tab
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(innerPadding)
                    .imePadding(),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 560.dp)
                        .padding(horizontal = 20.dp)
                ) {
                    if (actionMessage != null || actionError != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        val isErr = actionError != null
                        GlassCard(
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (isErr) Icons.Rounded.ErrorOutline else Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = if (isErr) palette.errorCoral else palette.verifiedMint,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = actionError ?: actionMessage.orEmpty(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isErr) palette.errorCoral else palette.verifiedMint
                                    )
                                }
                                IconButton(
                                    onClick = onClearFeedback,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Tutup pesan",
                                        tint = palette.textSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "dashboard_tab_content",
                        modifier = Modifier.weight(1f)
                    ) { tab ->
                        when (tab) {
                            0 -> HomeIdentityTab(
                                profile = profile,
                                activitiesState = activitiesState,
                                onEditQuickClick = {
                                    selectedTab = 1
                                    isEditingProfile = true
                                }
                            )
                            1 -> PersonalDataTab(
                                profile = profile,
                                isEditing = isEditingProfile,
                                isSaving = isSaving,
                                onStartEdit = { isEditingProfile = true },
                                onCancelEdit = { isEditingProfile = false },
                                onSaveEdit = { draft ->
                                    onUpdateProfile(draft) {
                                        isEditingProfile = false
                                    }
                                }
                            )
                            else -> AccountSecurityTab(
                                firebaseUser = firebaseUser,
                                profile = profile,
                                isSaving = isSaving,
                                onDeleteProfile = onDeleteProfile,
                                onSignOutClick = onSignOutClick
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeIdentityTab(
    profile: UserProfile,
    activitiesState: UiState<List<ActivityLog>>,
    onEditQuickClick: () -> Unit
) {
    val palette = LocalGlassPalette.current
    val activityCount = (activitiesState as? UiState.Success)?.data?.size ?: 0

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 14.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Halo, ${profile.fullName.substringBefore(" ")} 👋",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Kartu Identitas Digital Terverifikasi",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textSecondary
                    )
                }
                GlassStatusPill(
                    text = "Aktif",
                    accentColor = palette.verifiedMint,
                    icon = Icons.Rounded.VerifiedUser
                )
            }
        }

        // Hero Glassmorphism Digital ID Card
        item {
            GlassCard(
                elevated = true,
                accentGlow = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("digital_id_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ProfileAvatar(
                        photoUrl = profile.photoUrl,
                        fullName = profile.fullName,
                        sizeDp = 68
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = profile.fullName,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "@${profile.username}",
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.cyanGlow
                        )
                        if (profile.occupation.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = profile.occupation,
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    IconButton(
                        onClick = onEditQuickClick,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(palette.surfaceMedium)
                            .border(1.dp, palette.subtleBorder, CircleShape)
                            .testTag("quick_edit_profile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Ubah data diri",
                            tint = palette.cyanGlow
                        )
                    }
                }

                if (profile.bio.isNotBlank()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "\"${profile.bio}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textSecondary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = palette.subtleBorder)
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IdMetricChip(
                        label = "Domisili",
                        value = profile.city,
                        modifier = Modifier.weight(1f)
                    )
                    IdMetricChip(
                        label = "ID Anggota",
                        value = "#${profile.userId.take(8).uppercase()}",
                        modifier = Modifier.weight(1f)
                    )
                    IdMetricChip(
                        label = "Terdaftar",
                        value = formatShortDate(profile.createdAt),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Quick Stats Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GlassCard(
                    contentPadding = PaddingValues(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CloudDone,
                        contentDescription = null,
                        tint = palette.cyanGlow,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "100%",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Kelengkapan Data",
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.textSecondary
                    )
                }

                GlassCard(
                    contentPadding = PaddingValues(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.History,
                        contentDescription = null,
                        tint = palette.violetGlow,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$activityCount",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Aktivitas Tercatat",
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.textSecondary
                    )
                }
            }
        }

        // Activity History Section
        item {
            Text(
                text = "Riwayat Aktivitas Akun",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        when (activitiesState) {
            is UiState.Loading -> {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = palette.cyanGlow,
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "Memuat riwayat aktivitas...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.textSecondary
                            )
                        }
                    }
                }
            }

            is UiState.Error -> {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = activitiesState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.errorCoral
                        )
                    }
                }
            }

            is UiState.Success -> {
                if (activitiesState.data.isEmpty()) {
                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.History,
                                    contentDescription = null,
                                    tint = palette.textMuted,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Belum ada riwayat aktivitas tercatat.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = palette.textSecondary
                                )
                            }
                        }
                    }
                } else {
                    items(activitiesState.data, key = { it.id }) { activity ->
                        ActivityLogCard(activity = activity)
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityLogCard(activity: ActivityLog) {
    val palette = LocalGlassPalette.current
    val (icon, tint) = when (activity.action) {
        "REGISTER" -> Icons.Rounded.HowToReg to palette.verifiedMint
        "UPDATE_PROFILE" -> Icons.Rounded.Edit to palette.violetGlow
        else -> Icons.AutoMirrored.Rounded.Login to palette.cyanGlow
    }

    GlassCard(
        shape = RoundedCornerShape(18.dp),
        contentPadding = PaddingValues(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = activity.title,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = activity.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textSecondary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatFullDateTime(activity.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textMuted
                )
            }
        }
    }
}

@Composable
private fun PersonalDataTab(
    profile: UserProfile,
    isEditing: Boolean,
    isSaving: Boolean,
    onStartEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onSaveEdit: (RegistrationDraft) -> Unit
) {
    val palette = LocalGlassPalette.current
    var editDraft by remember(profile, isEditing) {
        mutableStateOf(RegistrationDraft.fromProfile(profile))
    }
    var fieldErrors by remember(isEditing) { mutableStateOf<Map<String, String>>(emptyMap()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 14.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isEditing) "Ubah Data Diri" else "Data Diri Terdaftar",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (isEditing) "Perbarui informasi pribadi Anda di Firestore."
                    else "Informasi lengkap akun LuminaID Anda.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textSecondary
                )
            }
            if (!isEditing) {
                OutlinedButton(
                    onClick = onStartEdit,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("start_edit_profile_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ubah")
                }
            }
        }

        if (isEditing) {
            GlassCard(
                elevated = true,
                accentGlow = true,
                modifier = Modifier.fillMaxWidth()
            ) {
                RegistrationFormFields(
                    draft = editDraft,
                    errors = fieldErrors,
                    onDraftChange = { updated ->
                        editDraft = updated
                        if (fieldErrors.isNotEmpty()) {
                            fieldErrors = updated.validate()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                GlassPrimaryButton(
                    text = "Simpan Perubahan",
                    icon = Icons.Rounded.Save,
                    isLoading = isSaving,
                    onClick = {
                        val validation = editDraft.validate()
                        if (validation.isNotEmpty()) {
                            fieldErrors = validation
                        } else {
                            fieldErrors = emptyMap()
                            onSaveEdit(editDraft.sanitized())
                        }
                    },
                    testTag = "save_profile_changes_button"
                )

                Spacer(modifier = Modifier.height(10.dp))

                TextButton(
                    onClick = onCancelEdit,
                    enabled = !isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("cancel_edit_profile_button")
                ) {
                    Text(
                        text = "Batal",
                        color = palette.textSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        } else {
            GlassCard(
                elevated = true,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Identitas Utama",
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.cyanGlow
                )
                Spacer(modifier = Modifier.height(12.dp))
                ProfileInfoRow(Icons.Rounded.Person, "Nama Lengkap", profile.fullName)
                ProfileInfoRow(Icons.Rounded.AlternateEmail, "Username", "@${profile.username}")
                ProfileInfoRow(Icons.Rounded.Wc, "Jenis Kelamin", profile.gender)
                ProfileInfoRow(Icons.Rounded.CalendarMonth, "Tanggal Lahir", profile.birthDate)
            }

            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Kontak & Domisili",
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.violetGlow
                )
                Spacer(modifier = Modifier.height(12.dp))
                ProfileInfoRow(Icons.Rounded.Email, "Email Google", profile.email.ifBlank { "-" })
                ProfileInfoRow(Icons.Rounded.Phone, "Nomor Telepon", profile.phone)
                ProfileInfoRow(Icons.Rounded.LocationCity, "Kota / Domisili", profile.city)
            }

            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Profesi & Tentang Saya",
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.verifiedMint
                )
                Spacer(modifier = Modifier.height(12.dp))
                ProfileInfoRow(
                    Icons.Rounded.WorkOutline,
                    "Pekerjaan",
                    profile.occupation.ifBlank { "Belum diisi" }
                )
                ProfileInfoRow(
                    Icons.AutoMirrored.Rounded.Notes,
                    "Bio Singkat",
                    profile.bio.ifBlank { "Belum diisi" },
                    showDivider = false
                )
            }
        }
    }
}

@Composable
private fun AccountSecurityTab(
    firebaseUser: FirebaseUser,
    profile: UserProfile,
    isSaving: Boolean,
    onDeleteProfile: () -> Unit,
    onSignOutClick: () -> Unit
) {
    val palette = LocalGlassPalette.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 14.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Akun & Keamanan",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        GlassCard(
            elevated = true,
            accentGlow = true,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ProfileAvatar(
                    photoUrl = profile.photoUrl ?: firebaseUser.photoUrl?.toString(),
                    fullName = profile.fullName,
                    sizeDp = 56
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profile.fullName,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = firebaseUser.email ?: "Terhubung dengan Google",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = palette.subtleBorder)
            Spacer(modifier = Modifier.height(14.dp))

            ProfileInfoRow(
                icon = Icons.Rounded.VerifiedUser,
                label = "Metode Autentikasi",
                value = "Google Sign-In (Firebase Auth)"
            )
            ProfileInfoRow(
                icon = Icons.Rounded.Fingerprint,
                label = "Firebase UID",
                value = firebaseUser.uid
            )
            ProfileInfoRow(
                icon = Icons.Rounded.Shield,
                label = "Terakhir Diperbarui",
                value = formatFullDateTime(profile.updatedAt),
                showDivider = false
            )
        }

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Manajemen Sesi",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Keluar dari sesi saat ini atau hapus data registrasi Anda dari Cloud Firestore.",
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            GlassPrimaryButton(
                text = "Keluar dari Akun",
                icon = Icons.AutoMirrored.Rounded.Logout,
                onClick = onSignOutClick,
                enabled = !isSaving,
                testTag = "sign_out_button"
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { showDeleteDialog = true },
                enabled = !isSaving,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = palette.errorCoral),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .minimumInteractiveComponentSize()
                    .testTag("delete_profile_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Hapus Data Diri",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Data Diri?") },
            text = {
                Text("Data profil Anda di Cloud Firestore akan dihapus. Anda dapat mendaftarkannya kembali kapan saja setelah masuk ulang.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteProfile()
                    }
                ) {
                    Text("Hapus", color = palette.errorCoral, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun ProfileAvatar(
    photoUrl: String?,
    fullName: String,
    sizeDp: Int
) {
    val palette = LocalGlassPalette.current
    if (!photoUrl.isNullOrBlank()) {
        AsyncImage(
            model = photoUrl,
            contentDescription = "Foto profil $fullName",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(sizeDp.dp)
                .clip(CircleShape)
                .border(2.dp, palette.cyanGlow, CircleShape)
        )
    } else {
        val initials = fullName
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifBlank { "L" }
        Box(
            modifier = Modifier
                .size(sizeDp.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(listOf(palette.cyanGlow, palette.violetGlow))
                )
                .border(2.dp, palette.highlightBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = palette.onAccentDark
            )
        }
    }
}

@Composable
private fun IdMetricChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val palette = LocalGlassPalette.current
    Column(modifier = modifier.padding(end = 6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = palette.textMuted
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    showDivider: Boolean = true
) {
    val palette = LocalGlassPalette.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = palette.cyanGlow,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(18.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textMuted
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
        if (showDivider) {
            HorizontalDivider(color = palette.subtleBorder.copy(alpha = 0.6f))
        }
    }
}

private fun formatShortDate(timestamp: Timestamp?): String {
    val date = timestamp?.toDate() ?: return "Baru"
    val formatter = SimpleDateFormat("dd MMM yy", Locale.forLanguageTag("id-ID"))
    return formatter.format(date)
}

private fun formatFullDateTime(timestamp: Timestamp?): String {
    val date = timestamp?.toDate() ?: return "Baru saja"
    val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID"))
    return formatter.format(date)
}
