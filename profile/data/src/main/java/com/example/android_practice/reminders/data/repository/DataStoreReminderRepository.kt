package com.example.android_practice.reminders.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.android_practice.reminders.domain.model.ClassReminder
import com.example.android_practice.reminders.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class DataStoreReminderRepository(private val preferences: DataStore<Preferences>) : ReminderRepository {
    override val reminder = preferences.data.map { values ->
        val triggerAt = values[TRIGGER_AT]
        val ownerName = values[OWNER_NAME]
        if (triggerAt != null && ownerName != null) ClassReminder(triggerAt, ownerName) else null
    }.distinctUntilChanged()

    override suspend fun save(reminder: ClassReminder) {
        preferences.edit { values ->
            values[TRIGGER_AT] = reminder.triggerAtMillis
            values[OWNER_NAME] = reminder.ownerName
        }
    }

    override suspend fun clear() {
        preferences.edit { values ->
            values.remove(TRIGGER_AT)
            values.remove(OWNER_NAME)
        }
    }

    private companion object {
        val TRIGGER_AT = longPreferencesKey("reminders.class.trigger_at")
        val OWNER_NAME = stringPreferencesKey("reminders.class.owner_name")
    }
}
