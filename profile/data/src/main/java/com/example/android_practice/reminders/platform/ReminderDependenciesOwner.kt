package com.example.android_practice.reminders.platform

import com.example.android_practice.reminders.domain.repository.ReminderDelivery
import kotlinx.coroutines.CoroutineScope

interface ReminderDependenciesOwner {
    val reminderDelivery: ReminderDelivery
    val reminderScope: CoroutineScope
}
