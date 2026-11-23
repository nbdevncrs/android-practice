package com.example.android_practice.reminders.platform

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.android_practice.PracticeApplication
import com.example.android_practice.reminders.data.AndroidReminderScheduler
import java.io.IOException
import kotlinx.coroutines.launch

class ClassReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AndroidReminderScheduler.ACTION_REMIND) return
        val pendingResult = goAsync()
        val container = (context.applicationContext as PracticeApplication).container
        container.applicationScope.launch {
            try {
                container.reminderScheduler.deliver()
            } catch (error: IOException) {
                Log.e("ClassReminder", "Unable to read the reminder", error)
            } catch (error: SecurityException) {
                Log.e("ClassReminder", "Unable to show the reminder", error)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
