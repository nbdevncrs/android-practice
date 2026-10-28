package com.example.android_practice.profile.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.example.android_practice.catalog.data.repository.DataStoreCatalogFiltersRepository
import com.example.android_practice.catalog.domain.model.CatalogFilters
import com.example.android_practice.profile.domain.model.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class DataStoreProfileRepositoryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun missingProfileUsesEmptyValues() = runTest {
        val repository = DataStoreProfileRepository(InMemoryStore())

        assertEquals(UserProfile(), repository.profile.first())
    }

    @Test
    fun profileSurvivesClosingAndReopeningStorage() = runTest {
        val file = temporaryFolder.root.resolve("profile.preferences_pb").absolutePath.toPath()
        val firstJob = Job()
        val firstStore = PreferenceDataStoreFactory.createWithPath(
            scope = CoroutineScope(firstJob + StandardTestDispatcher(testScheduler)),
            produceFile = { file },
        )
        val expected = UserProfile("Alex Morgan", "content://example/Photos/Avatar", "https://example.com/MyResume.pdf")
        try {
            DataStoreProfileRepository(firstStore).save(UserProfile(
                " Alex Morgan ", " content://example/Photos/Avatar ", " https://example.com/MyResume.pdf ",
            ))
        } finally {
            firstJob.cancelAndJoin()
        }

        val reopenedStore = PreferenceDataStoreFactory.createWithPath(scope = backgroundScope) { file }
        assertEquals(expected, DataStoreProfileRepository(reopenedStore).profile.first())
    }

    @Test
    fun savingAndClearingProfilePreservesCatalogSettings() = runTest {
        val preferences = InMemoryStore()
        val filters = DataStoreCatalogFiltersRepository(preferences)
        val repository = DataStoreProfileRepository(preferences)
        val appliedFilters = CatalogFilters("pika", "electric")
        filters.save(appliedFilters)
        repository.save(UserProfile("Alex", "file:///avatar.jpg", "https://example.com/resume.pdf"))
        repository.save(UserProfile(" ", " ", " "))

        assertEquals(UserProfile(), repository.profile.first())
        assertEquals(appliedFilters, filters.filters.first())
    }

    @Test
    fun observersReceiveWholeProfileWithoutIntermediateValuesOrDuplicates() = runTest {
        val repository = DataStoreProfileRepository(InMemoryStore())
        val received = mutableListOf<UserProfile>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.profile.collect { received += it }
        }
        val profile = UserProfile("Alex", "file:///avatar.jpg", "https://example.com/resume.pdf")
        repository.save(profile)
        repository.save(profile)

        assertEquals(listOf(UserProfile(), profile), received)
    }

    @Test
    fun failedSaveDoesNotReplacePreviouslySavedProfile() = runTest {
        val preferences = InMemoryStore()
        val repository = DataStoreProfileRepository(preferences)
        val original = UserProfile(fullName = "Alex")
        repository.save(original)
        val failure = IOException("Storage unavailable")
        preferences.writeError = failure
        try {
            repository.save(UserProfile(fullName = "Updated"))
            fail("Expected storage error")
        } catch (error: IOException) {
            assertSame(failure, error)
        }

        assertEquals(original, repository.profile.first())
    }

    private class InMemoryStore : DataStore<Preferences> {
        override val data = MutableStateFlow(emptyPreferences())
        private val mutex = Mutex()
        var writeError: IOException? = null

        override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences =
            mutex.withLock {
                writeError?.let { throw it }
                transform(data.value).also { data.value = it }
            }
    }
}
