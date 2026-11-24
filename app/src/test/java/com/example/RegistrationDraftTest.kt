package com.example

import com.example.data.model.RegistrationDraft
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RegistrationDraftTest {

    @Test
    fun `valid draft produces no validation errors`() {
        val draft = RegistrationDraft(
            fullName = "  Dimas Pratama  ",
            username = "Dimas.Pratama_01",
            phone = "081234567890",
            birthDate = "14/08/1998",
            gender = "Laki-laki",
            city = "Jakarta Selatan",
            occupation = "Android Developer",
            bio = "Suka desain UI modern."
        )
        val sanitized = draft.sanitized()
        assertTrue(sanitized.validate().isEmpty())
        assertEquals("Dimas Pratama", sanitized.fullName)
        assertEquals("dimas.pratama_01", sanitized.username)
    }

    @Test
    fun `invalid draft catches short fields and bad username`() {
        val draft = RegistrationDraft(
            fullName = "A",
            username = "a b",
            phone = "123",
            birthDate = "",
            gender = "Invalid",
            city = "J"
        )
        val errors = draft.validate()
        assertTrue(errors.containsKey("fullName"))
        assertTrue(errors.containsKey("username"))
        assertTrue(errors.containsKey("phone"))
        assertTrue(errors.containsKey("birthDate"))
        assertTrue(errors.containsKey("gender"))
        assertTrue(errors.containsKey("city"))
    }
}
