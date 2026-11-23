package com.example.android_practice.reminders.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.android_practice.reminders.domain.model.ClassReminder
import com.example.android_practice.reminders.domain.model.ReminderError
import com.example.android_practice.reminders.domain.model.ReminderException
import com.example.android_practice.reminders.domain.repository.ReminderClock
import com.example.android_practice.reminders.domain.repository.ReminderRepository
import com.example.android_practice.reminders.domain.repository.ReminderScheduler
import com.example.android_practice.reminders.domain.repository.ReminderAccess
import com.example.android_practice.reminders.domain.repository.ReminderDelivery
import com.example.android_practice.reminders.platform.ClassReminderNotifications
import com.example.android_practice.reminders.platform.ClassReminderReceiver
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

internal class AndroidReminderScheduler(
    private val context: Context,
    private val repository: ReminderRepository,
    private val clock: ReminderClock,
    private val notifications: ClassReminderNotifications,
    private val access: ReminderAccess,
) : ReminderScheduler, ReminderDelivery {
    private val alarms = context.getSystemService(AlarmManager::class.java)
    private val mutex = Mutex()

    override suspend fun schedule(reminder: ClassReminder) = withContext(Dispatchers.IO + NonCancellable) {
        mutex.withLock {
            if (!access.notificationsAllowed()) throw ReminderException(ReminderError.NOTIFICATIONS_DISABLED)
            if (!access.exactAlarmsAllowed()) {
                throw ReminderException(ReminderError.EXACT_ALARM_NOT_ALLOWED)
            }
            if (reminder.triggerAtMillis <= clock.nowMillis()) throw ReminderException(ReminderError.TIME_PASSED)
            val previous = try {
                val savedReminder = repository.reminder.first()
                repository.save(reminder)
                savedReminder
            } catch (error: IOException) {
                throw ReminderException(ReminderError.STORAGE, error)
            }
            try {
                alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminder.triggerAtMillis, alarmIntent())
            } catch (error: RuntimeException) {
                try {
                    if (previous == null) repository.clear() else repository.save(previous)
                } catch (storageError: IOException) {
                    error.addSuppressed(storageError)
                }
                val reason = if (error is SecurityException) ReminderError.EXACT_ALARM_NOT_ALLOWED
                    else ReminderError.SCHEDULING
                throw ReminderException(reason, error)
            }
        }
    }

    override suspend fun cancel() = withContext(Dispatchers.IO + NonCancellable) {
        mutex.withLock {
            try {
                repository.clear()
                alarms.cancel(alarmIntent())
            } catch (error: IOException) {
                throw ReminderException(ReminderError.STORAGE, error)
            }
        }
    }

    override suspend fun deliver() = withContext(Dispatchers.IO) {
        mutex.withLock {
            val reminder = repository.reminder.first() ?: return@withLock
            if (reminder.triggerAtMillis > clock.nowMillis()) return@withLock
            notifications.show(reminder)
            repository.clear()
        }
    }

    private fun alarmIntent(): PendingIntent = PendingIntent.getBroadcast(
        context,
        ALARM_REQUEST_CODE,
        Intent(context, ClassReminderReceiver::class.java).setAction(ACTION_REMIND),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    companion object {
        const val ACTION_REMIND = "com.example.android_practice.CLASS_REMINDER"
        private const val ALARM_REQUEST_CODE = 701
    }
}
