package com.example.android_practice.reminders.domain.repository

fun interface ReminderDelivery {
    suspend fun deliver()
}
