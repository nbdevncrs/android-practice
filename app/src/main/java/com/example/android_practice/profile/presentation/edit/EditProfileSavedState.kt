package com.example.android_practice.profile.presentation.edit

import androidx.lifecycle.SavedStateHandle
import com.example.android_practice.profile.domain.model.UserProfile

internal class EditProfileSavedState(private val handle: SavedStateHandle) {
    val draft: UserProfile?
        get() = if (handle.get<Boolean>(DRAFT_INITIALIZED) == true) {
            UserProfile(handle[FULL_NAME] ?: "", handle[AVATAR_URI], handle[RESUME_URL] ?: "")
        } else null

    val pendingCameraUri: String? get() = handle[CAMERA_URI]
    val cameraToLaunch: String?
        get() = if (handle.get<Boolean>(CAMERA_LAUNCHED) == true) null else pendingCameraUri

    fun saveDraft(profile: UserProfile) {
        handle[DRAFT_INITIALIZED] = true
        handle[FULL_NAME] = profile.fullName
        handle[AVATAR_URI] = profile.avatarUri
        handle[RESUME_URL] = profile.resumeUrl
    }

    fun prepareCamera(uri: String) {
        handle[CAMERA_URI] = uri
        handle[CAMERA_LAUNCHED] = false
    }

    fun cameraLaunched() {
        handle[CAMERA_LAUNCHED] = true
    }

    fun clearCamera() {
        handle.remove<String>(CAMERA_URI)
        handle.remove<Boolean>(CAMERA_LAUNCHED)
    }

    private companion object {
        const val DRAFT_INITIALIZED = "profile.edit.initialized"
        const val FULL_NAME = "profile.edit.full_name"
        const val AVATAR_URI = "profile.edit.avatar_uri"
        const val RESUME_URL = "profile.edit.resume_url"
        const val CAMERA_URI = "profile.edit.camera_uri"
        const val CAMERA_LAUNCHED = "profile.edit.camera_launched"
    }
}
