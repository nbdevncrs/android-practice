package com.example.android_practice

import android.app.Application
import com.example.android_practice.reminders.domain.repository.ReminderDelivery
import com.example.android_practice.reminders.platform.ReminderDependenciesOwner
import kotlinx.coroutines.CoroutineScope

class PracticeApplication : Application(), ReminderDependenciesOwner {
    val container: AppContainer by lazy { AppContainer(this) }
    override val reminderDelivery: ReminderDelivery get() = container.profileData.reminderDelivery
    override val reminderScope: CoroutineScope get() = container.applicationScope
}
