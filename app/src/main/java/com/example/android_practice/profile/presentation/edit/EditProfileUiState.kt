package com.example.android_practice.profile.presentation.edit

import com.example.android_practice.profile.domain.model.ProfileValidation
import com.example.android_practice.profile.domain.model.UserProfile

enum class EditProfileError { LOAD, SAVE, AVATAR, CAMERA }

data class EditProfileUiState(
    val draft: UserProfile = UserProfile(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val isImportingAvatar: Boolean = false,
    val cameraToLaunch: String? = null,
    val error: EditProfileError? = null,
    val validation: ProfileValidation = ProfileValidation(),
) {
    val canEdit: Boolean get() = !isLoading && !isSaving && !isSaved && !isImportingAvatar &&
        cameraToLaunch == null && error != EditProfileError.LOAD
}
