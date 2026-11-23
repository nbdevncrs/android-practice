package com.example.android_practice.profile.presentation.edit

import com.example.android_practice.profile.domain.model.ProfileValidation
import com.example.android_practice.profile.domain.model.UserProfile
import com.example.android_practice.profile.domain.model.ClassTimeValidation
import com.example.android_practice.profile.domain.model.validateClassTime
import com.example.android_practice.profile.domain.usecase.SaveProfileResult
import com.example.android_practice.reminders.domain.model.ReminderError

internal enum class EditProfileError { LOAD, SAVE, AVATAR, CAMERA, NOTIFICATIONS, EXACT_ALARM, SETTINGS, TIME_PASSED, REMINDER }

internal data class EditProfileUiState(
    val draft: UserProfile = UserProfile(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val isProfileSavedWithoutReminder: Boolean = false,
    val isImportingAvatar: Boolean = false,
    val isRequestingReminderAccess: Boolean = false,
    val cameraToLaunch: String? = null,
    val error: EditProfileError? = null,
    val validation: ProfileValidation = ProfileValidation(),
) {
    val timeValidation: ClassTimeValidation get() = validateClassTime(draft.favoriteClassTime)
    val isBusy: Boolean get() = isSaving || isImportingAvatar || isRequestingReminderAccess
    val canSave: Boolean get() = canEdit && timeValidation == ClassTimeValidation.VALID
    val canEdit: Boolean get() = !isLoading && !isBusy && !isSaved &&
        cameraToLaunch == null && error != EditProfileError.LOAD
}

internal fun EditProfileUiState.withSaveResult(result: SaveProfileResult): EditProfileUiState {
    val idle = copy(isSaving = false, error = null)
    return when (result) {
        SaveProfileResult.Saved -> idle.copy(isSaved = true, isProfileSavedWithoutReminder = false)
        is SaveProfileResult.InvalidProfile -> idle.copy(validation = result.validation)
        SaveProfileResult.TimePassed -> idle.copy(error = EditProfileError.TIME_PASSED)
        SaveProfileResult.StorageFailure -> idle.copy(error = EditProfileError.SAVE)
        is SaveProfileResult.ReminderFailure -> idle.copy(
            isProfileSavedWithoutReminder = true,
            error = result.error.toEditProfileError(),
        )
    }
}

private fun ReminderError.toEditProfileError(): EditProfileError = when (this) {
    ReminderError.NOTIFICATIONS_DISABLED -> EditProfileError.NOTIFICATIONS
    ReminderError.EXACT_ALARM_NOT_ALLOWED -> EditProfileError.EXACT_ALARM
    ReminderError.TIME_PASSED -> EditProfileError.TIME_PASSED
    ReminderError.STORAGE, ReminderError.SCHEDULING -> EditProfileError.REMINDER
}
