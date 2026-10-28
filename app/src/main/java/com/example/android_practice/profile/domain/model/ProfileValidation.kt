package com.example.android_practice.profile.domain.model

import java.net.URI
import java.net.URISyntaxException

data class ProfileValidation(
    val fullNameRequired: Boolean = false,
    val invalidResumeUrl: Boolean = false,
) {
    val isValid: Boolean get() = !fullNameRequired && !invalidResumeUrl
}

fun UserProfile.validate(): ProfileValidation {
    val normalized = normalized()
    val validUrl = normalized.resumeUrl.isEmpty() || try {
        val uri = URI(normalized.resumeUrl)
        (uri.scheme.equals("https", ignoreCase = true) || uri.scheme.equals("http", ignoreCase = true)) &&
            !uri.host.isNullOrBlank() && uri.port in -1..65535
    } catch (_: URISyntaxException) {
        false
    }
    return ProfileValidation(
        fullNameRequired = normalized.fullName.isEmpty(),
        invalidResumeUrl = !validUrl,
    )
}
