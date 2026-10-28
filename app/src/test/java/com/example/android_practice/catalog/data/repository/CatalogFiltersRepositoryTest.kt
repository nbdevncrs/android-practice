package com.example.android_practice.catalog.data.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.android_practice.catalog.domain.model.CatalogFilters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class CatalogFiltersRepositoryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun emptyStorageUsesDefaultFilters() = runTest {
        val store = PreferenceDataStoreFactory.createWithPath(scope = backgroundScope) {
            temporaryFolder.root.resolve("filters.preferences_pb").absolutePath.toPath()
        }
        val filters = DataStoreCatalogFiltersRepository(store).filters.first()

        assertEquals(CatalogFilters(), filters)
        assertFalse(filters.isActive)
    }

    @Test
    fun savedFiltersSurviveClosingAndReopeningStorage() = runTest {
        val file = temporaryFolder.root.resolve("filters.preferences_pb").absolutePath.toPath()
        val firstJob = Job()
        val firstStore = PreferenceDataStoreFactory.createWithPath(
            scope = CoroutineScope(firstJob + StandardTestDispatcher(testScheduler)),
            produceFile = { file },
        )
        try {
            DataStoreCatalogFiltersRepository(firstStore).save(CatalogFilters("  PiKa  ", " ELECTRIC "))
        } finally {
            firstJob.cancelAndJoin()
        }

        val reopenedStore = PreferenceDataStoreFactory.createWithPath(scope = backgroundScope) { file }
        assertEquals(
            CatalogFilters("pika", "electric"),
            DataStoreCatalogFiltersRepository(reopenedStore).filters.first(),
        )
    }

    @Test
    fun resetRemovesOnlyFilterSettings() = runTest {
        val store = InMemoryPreferencesStore()
        val unrelatedKey = stringPreferencesKey("unrelated_setting")
        store.edit { it[unrelatedKey] = "keep" }
        val repository = DataStoreCatalogFiltersRepository(store)
        repository.save(CatalogFilters("pika", "electric"))

        repository.reset()

        assertEquals(CatalogFilters(), repository.filters.first())
        assertEquals("keep", store.data.first()[unrelatedKey])
    }

    @Test
    fun blankInputRestoresDefaultState() = runTest {
        val store = PreferenceDataStoreFactory.createWithPath(scope = backgroundScope) {
            temporaryFolder.root.resolve("filters.preferences_pb").absolutePath.toPath()
        }
        val repository = DataStoreCatalogFiltersRepository(store)
        repository.save(CatalogFilters("  ", "  "))

        assertEquals(CatalogFilters(), repository.filters.first())
    }

    private class InMemoryPreferencesStore : DataStore<Preferences> {
        override val data = MutableStateFlow(emptyPreferences())
        private val mutex = Mutex()

        override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences =
            mutex.withLock {
                transform(data.value).also { data.value = it }
            }
    }
}
