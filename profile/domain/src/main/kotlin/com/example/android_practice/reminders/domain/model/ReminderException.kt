package com.example.android_practice.reminders.domain.model

enum class ReminderError { NOTIFICATIONS_DISABLED, EXACT_ALARM_NOT_ALLOWED, TIME_PASSED, STORAGE, SCHEDULING }

class ReminderException(val error: ReminderError, cause: Throwable? = null) : Exception(cause)
