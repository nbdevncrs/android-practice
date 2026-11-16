package com.example.android_practice.reminders.domain.repository

import com.example.android_practice.profile.domain.model.ClassTime

interface ReminderClock {
    fun nowMillis(): Long
    fun todayAt(time: ClassTime): Long
}
