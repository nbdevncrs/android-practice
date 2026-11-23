package com.example.android_practice.reminders.data

import com.example.android_practice.profile.domain.model.ClassTime
import com.example.android_practice.reminders.domain.repository.ReminderClock
import java.util.Calendar

internal class SystemReminderClock : ReminderClock {
    override fun nowMillis(): Long = System.currentTimeMillis()

    override fun todayAt(time: ClassTime): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, time.hour)
        set(Calendar.MINUTE, time.minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
