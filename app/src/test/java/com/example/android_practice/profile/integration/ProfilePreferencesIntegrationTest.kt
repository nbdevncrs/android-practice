package com.example.android_practice.profile.integration

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.example.android_practice.catalog.data.repository.DataStoreCatalogFiltersRepository
import com.example.android_practice.catalog.domain.model.CatalogFilters
import com.example.android_practice.profile.data.repository.DataStoreProfileRepository
import com.example.android_practice.profile.domain.model.UserProfile
import com.example.android_practice.reminders.data.repository.DataStoreReminderRepository
import com.example.android_practice.reminders.domain.model.ClassReminder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ProfilePreferencesIntegrationTest {
    @Test
    fun profileAndReminderWritesPreserveCatalogSettings() = runTest {
        val preferences = object : DataStore<Preferences> {
            override val data = MutableStateFlow(emptyPreferences())
            private val mutex = Mutex()

            override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences =
                mutex.withLock { transform(data.value).also { data.value = it } }
        }
        val filters = DataStoreCatalogFiltersRepository(preferences)
        val profile = DataStoreProfileRepository(preferences)
        val reminders = DataStoreReminderRepository(preferences)
        val expected = CatalogFilters("pika", "electric")

        filters.save(expected)
        profile.save(UserProfile("Alex", favoriteClassTime = "09:05"))
        reminders.save(ClassReminder(32_700_000L, "Alex"))
        profile.save(UserProfile())
        reminders.clear()

        assertEquals(expected, filters.filters.first())
    }
}
