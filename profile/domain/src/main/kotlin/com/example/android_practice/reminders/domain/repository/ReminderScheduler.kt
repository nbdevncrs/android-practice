package com.example.android_practice.reminders.domain.repository

import com.example.android_practice.reminders.domain.model.ClassReminder

interface ReminderScheduler {
    suspend fun schedule(reminder: ClassReminder)
    suspend fun cancel()
}
