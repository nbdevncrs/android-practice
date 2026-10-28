package com.example.android_practice.profile.domain.model

data class UserProfile(
    val fullName: String = "",
    val avatarUri: String? = null,
    val resumeUrl: String = "",
) {
    fun normalized(): UserProfile = copy(
        fullName = fullName.trim(),
        avatarUri = avatarUri?.trim()?.takeIf { it.isNotEmpty() },
        resumeUrl = resumeUrl.trim(),
    )
}
