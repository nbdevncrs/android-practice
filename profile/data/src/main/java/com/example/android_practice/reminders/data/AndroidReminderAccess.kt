package com.example.android_practice.reminders.data

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import com.example.android_practice.reminders.domain.repository.ReminderAccess
import com.example.android_practice.reminders.platform.ClassReminderNotifications

internal class AndroidReminderAccess(
    context: Context,
    private val notifications: ClassReminderNotifications,
) : ReminderAccess {
    private val alarms = context.applicationContext.getSystemService(AlarmManager::class.java)

    override fun notificationsAllowed(): Boolean = notifications.isAllowed()

    override fun exactAlarmsAllowed(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarms.canScheduleExactAlarms()
}
