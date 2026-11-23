package com.example.android_practice.profile.data

import android.app.PendingIntent
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.android_practice.profile.data.repository.DataStoreProfileRepository
import com.example.android_practice.profile.data.repository.FileAvatarRepository
import com.example.android_practice.profile.data.repository.HttpResumeRepository
import com.example.android_practice.profile.domain.repository.AvatarRepository
import com.example.android_practice.profile.domain.repository.ProfileRepository
import com.example.android_practice.profile.domain.repository.ResumeRepository
import com.example.android_practice.reminders.data.AndroidReminderAccess
import com.example.android_practice.reminders.data.AndroidReminderScheduler
import com.example.android_practice.reminders.data.SystemReminderClock
import com.example.android_practice.reminders.data.repository.DataStoreReminderRepository
import com.example.android_practice.reminders.domain.repository.ReminderAccess
import com.example.android_practice.reminders.domain.repository.ReminderClock
import com.example.android_practice.reminders.domain.repository.ReminderDelivery
import com.example.android_practice.reminders.domain.repository.ReminderScheduler
import com.example.android_practice.reminders.platform.ClassReminderNotifications
import java.io.File
import okhttp3.OkHttpClient

class ProfileData(
    context: Context,
    preferences: DataStore<Preferences>,
    client: OkHttpClient,
    openApplication: () -> PendingIntent,
) {
    private val applicationContext = context.applicationContext
    val profiles: ProfileRepository = DataStoreProfileRepository(preferences)
    val avatars: AvatarRepository = FileAvatarRepository(applicationContext)
    val resumes: ResumeRepository = HttpResumeRepository(File(applicationContext.cacheDir, "profile/resumes"), client)
    val clock: ReminderClock = SystemReminderClock()

    private val notifications = ClassReminderNotifications(applicationContext, openApplication)
    val reminderAccess: ReminderAccess = AndroidReminderAccess(applicationContext, notifications)
    private val reminders = AndroidReminderScheduler(
        applicationContext, DataStoreReminderRepository(preferences), clock, notifications, reminderAccess,
    )
    val reminderScheduler: ReminderScheduler get() = reminders
    val reminderDelivery: ReminderDelivery get() = reminders
}
