package com.example.android_practice.profile.presentation.edit

import androidx.lifecycle.SavedStateHandle
import com.example.android_practice.profile.domain.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Test

class EditProfileSavedStateTest {
    @Test
    fun incompleteTimeDraftSurvivesRecreationWithoutNormalization() {
        val handle = SavedStateHandle()
        val profile = UserProfile(fullName = "Alex", favoriteClassTime = "09:")
        EditProfileSavedState(handle).saveDraft(profile)
        val restoredHandle = SavedStateHandle(handle.keys().associateWith { handle.get<Any?>(it) })

        assertEquals(profile, EditProfileSavedState(restoredHandle).draft)
    }

    @Test
    fun oldDraftWithoutTimeRestoresWithEmptyTime() {
        val handle = SavedStateHandle(mapOf("profile.edit.initialized" to true, "profile.edit.full_name" to "Alex"))

        assertEquals(UserProfile(fullName = "Alex"), EditProfileSavedState(handle).draft)
    }
}
