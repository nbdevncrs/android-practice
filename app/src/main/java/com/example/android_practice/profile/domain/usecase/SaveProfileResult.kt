package com.example.android_practice.profile.domain.usecase

import com.example.android_practice.profile.domain.model.ClassTimeValidation
import com.example.android_practice.profile.domain.model.ProfileValidation
import com.example.android_practice.reminders.domain.model.ReminderError

sealed interface SaveProfileResult {
    data object Saved : SaveProfileResult
    data class InvalidProfile(val validation: ProfileValidation, val time: ClassTimeValidation) : SaveProfileResult
    data object TimePassed : SaveProfileResult
    data object StorageFailure : SaveProfileResult
    data class ReminderFailure(val error: ReminderError) : SaveProfileResult
}
