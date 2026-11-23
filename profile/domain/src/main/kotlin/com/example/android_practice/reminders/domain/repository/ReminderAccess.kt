package com.example.android_practice.reminders.domain.repository

interface ReminderAccess {
    fun notificationsAllowed(): Boolean
    fun exactAlarmsAllowed(): Boolean
}
