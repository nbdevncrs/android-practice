package com.example.android_practice.reminders.domain.repository

import com.example.android_practice.reminders.domain.model.ClassReminder
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    val reminder: Flow<ClassReminder?>
    suspend fun save(reminder: ClassReminder)
    suspend fun clear()
}
