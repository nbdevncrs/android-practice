package com.example.android_practice.profile.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileValidationTest {
    @Test
    fun singleNameAndOptionalResumeAreAccepted() {
        assertTrue(UserProfile(fullName = " Alex ").validate().isValid)
        assertTrue(UserProfile("Alex", resumeUrl = " HTTPS://example.com/MyResume.pdf?download=1 ").validate().isValid)
    }

    @Test
    fun blankNameIsRejected() {
        assertTrue(UserProfile(fullName = "  ").validate().fullNameRequired)
    }

    @Test
    fun malformedAndNonHttpResumeLinksAreRejected() {
        listOf("resume.pdf", "file:///resume.pdf", "javascript:alert(1)", "https://", "https://example.com/my resume.pdf", "https://example.com:70000/a.pdf")
            .forEach { url ->
                assertFalse(url, UserProfile("Alex", resumeUrl = url).validate().isValid)
            }
    }
}
