package com.example.android_practice.profile.presentation.navigation

import com.example.android_practice.profile.domain.repository.AvatarRepository
import com.example.android_practice.profile.domain.repository.ProfileRepository
import com.example.android_practice.profile.domain.repository.ResumeRepository
import com.example.android_practice.profile.domain.usecase.DiscardUnusedAvatar
import com.example.android_practice.profile.domain.usecase.SaveProfileAndScheduleReminder
import com.example.android_practice.reminders.domain.repository.ReminderAccess
import kotlinx.coroutines.CoroutineScope

data class ProfileDependencies(
    val profiles: ProfileRepository,
    val avatars: AvatarRepository,
    val resumes: ResumeRepository,
    val reminderAccess: ReminderAccess,
    val discardUnusedAvatar: DiscardUnusedAvatar,
    val saveProfileAndScheduleReminder: SaveProfileAndScheduleReminder,
    val cleanupScope: CoroutineScope,
)
