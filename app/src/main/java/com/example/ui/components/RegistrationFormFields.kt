package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.LocationCity
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.WorkOutline
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.RegistrationDraft
import com.example.ui.theme.LocalGlassPalette
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationFormFields(
    draft: RegistrationDraft,
    errors: Map<String, String>,
    onDraftChange: (RegistrationDraft) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalGlassPalette.current
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GlassTextField(
            value = draft.fullName,
            onValueChange = { onDraftChange(draft.copy(fullName = it)) },
            label = "Nama Lengkap *",
            placeholder = "cth. Dimas Pratama",
            leadingIcon = Icons.Rounded.Person,
            errorText = errors["fullName"],
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            testTag = "input_full_name"
        )

        GlassTextField(
            value = draft.username,
            onValueChange = { onDraftChange(draft.copy(username = it.replace(" ", ""))) },
            label = "Username *",
            placeholder = "cth. dimas.pratama",
            leadingIcon = Icons.Rounded.AlternateEmail,
            errorText = errors["username"],
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Ascii,
                imeAction = ImeAction.Next
            ),
            testTag = "input_username"
        )

        GlassTextField(
            value = draft.phone,
            onValueChange = { onDraftChange(draft.copy(phone = it)) },
            label = "Nomor Telepon / WhatsApp *",
            placeholder = "cth. 081234567890",
            leadingIcon = Icons.Rounded.Phone,
            errorText = errors["phone"],
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            ),
            testTag = "input_phone"
        )

        // Birth Date with Material 3 DatePickerDialog trigger + manual typing support
        GlassTextField(
            value = draft.birthDate,
            onValueChange = { onDraftChange(draft.copy(birthDate = it)) },
            label = "Tanggal Lahir *",
            placeholder = "DD/MM/YYYY",
            leadingIcon = Icons.Rounded.CalendarMonth,
            errorText = errors["birthDate"],
            trailingContent = {
                IconButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.testTag("pick_birth_date_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CalendarMonth,
                        contentDescription = "Pilih tanggal lahir",
                        tint = palette.cyanGlow
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            testTag = "input_birth_date"
        )

        // Gender selector chips
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Jenis Kelamin *",
                style = MaterialTheme.typography.labelMedium,
                color = palette.textSecondary,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RegistrationDraft.ALLOWED_GENDERS.forEach { option ->
                    val selected = draft.gender == option
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (selected) palette.cyanGlow.copy(alpha = 0.22f)
                                else palette.fieldContainer.copy(alpha = 0.55f)
                            )
                            .border(
                                width = 1.dp,
                                color = if (selected) palette.cyanGlow else palette.subtleBorder,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = palette.cyanGlow.copy(alpha = 0.3f)),
                                role = Role.RadioButton
                            ) {
                                onDraftChange(draft.copy(gender = option))
                            }
                            .testTag("gender_${option.lowercase().replace("-", "_")}")
                            .padding(vertical = 12.dp, horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) palette.cyanGlow else palette.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            if (errors["gender"] != null) {
                Text(
                    text = errors["gender"] ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.errorCoral,
                    modifier = Modifier.padding(start = 12.dp, top = 4.dp)
                )
            }
        }

        GlassTextField(
            value = draft.city,
            onValueChange = { onDraftChange(draft.copy(city = it)) },
            label = "Kota / Domisili *",
            placeholder = "cth. Jakarta Selatan",
            leadingIcon = Icons.Rounded.LocationCity,
            errorText = errors["city"],
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            testTag = "input_city"
        )

        GlassTextField(
            value = draft.occupation,
            onValueChange = { onDraftChange(draft.copy(occupation = it)) },
            label = "Pekerjaan / Profesi (Opsional)",
            placeholder = "cth. Software Engineer",
            leadingIcon = Icons.Rounded.WorkOutline,
            errorText = errors["occupation"],
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            testTag = "input_occupation"
        )

        GlassTextField(
            value = draft.bio,
            onValueChange = { onDraftChange(draft.copy(bio = it)) },
            label = "Bio Singkat (Opsional)",
            placeholder = "Ceritakan sedikit tentang diri Anda...",
            leadingIcon = Icons.AutoMirrored.Rounded.Notes,
            errorText = errors["bio"],
            singleLine = false,
            maxLines = 3,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done
            ),
            testTag = "input_bio"
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }
                            onDraftChange(draft.copy(birthDate = formatter.format(Date(millis))))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Pilih")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Batal")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
