package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val fullName: String = "",
    val username: String = "",
    val phone: String = "",
    val birthDate: String = "",
    val gender: String = "Laki-laki",
    val city: String = "",
    val occupation: String = "",
    val bio: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class RegistrationDraft(
    val fullName: String = "",
    val username: String = "",
    val phone: String = "",
    val birthDate: String = "",
    val gender: String = "Laki-laki",
    val city: String = "",
    val occupation: String = "",
    val bio: String = ""
) {
    fun validate(): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        val cleanName = fullName.trim()
        val cleanUsername = username.trim()
        val cleanPhone = phone.trim()
        val cleanBirthDate = birthDate.trim()
        val cleanCity = city.trim()
        val cleanOccupation = occupation.trim()
        val cleanBio = bio.trim()

        if (cleanName.length !in 2..60) {
            errors["fullName"] = "Nama lengkap harus 2–60 karakter."
        }
        if (cleanUsername.length !in 3..30 || !USERNAME_REGEX.matches(cleanUsername)) {
            errors["username"] = "Username 3–30 karakter (huruf, angka, titik, atau underscore)."
        }
        if (cleanPhone.length !in 6..20) {
            errors["phone"] = "Nomor telepon harus 6–20 karakter."
        }
        if (cleanBirthDate.length !in 4..20) {
            errors["birthDate"] = "Pilih atau masukkan tanggal lahir yang valid."
        }
        if (gender !in ALLOWED_GENDERS) {
            errors["gender"] = "Pilih jenis kelamin yang tersedia."
        }
        if (cleanCity.length !in 2..50) {
            errors["city"] = "Kota/domisili harus 2–50 karakter."
        }
        if (cleanOccupation.length > 60) {
            errors["occupation"] = "Pekerjaan maksimal 60 karakter."
        }
        if (cleanBio.length > 200) {
            errors["bio"] = "Bio singkat maksimal 200 karakter."
        }
        return errors
    }

    fun sanitized(): RegistrationDraft = copy(
        fullName = fullName.trim(),
        username = username.trim().lowercase(),
        phone = phone.trim(),
        birthDate = birthDate.trim(),
        city = city.trim(),
        occupation = occupation.trim(),
        bio = bio.trim()
    )

    companion object {
        val ALLOWED_GENDERS = listOf("Laki-laki", "Perempuan", "Lainnya")
        private val USERNAME_REGEX = Regex("^[a-zA-Z0-9_.]+$")

        fun fromProfile(profile: UserProfile): RegistrationDraft = RegistrationDraft(
            fullName = profile.fullName,
            username = profile.username,
            phone = profile.phone,
            birthDate = profile.birthDate,
            gender = if (profile.gender in ALLOWED_GENDERS) profile.gender else "Laki-laki",
            city = profile.city,
            occupation = profile.occupation,
            bio = profile.bio
        )
    }
}
