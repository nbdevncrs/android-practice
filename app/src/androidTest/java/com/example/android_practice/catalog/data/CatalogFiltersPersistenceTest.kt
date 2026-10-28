package com.example.android_practice.catalog.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.android_practice.catalog.data.repository.DataStoreCatalogFiltersRepository
import com.example.android_practice.catalog.domain.model.CatalogFilters
import com.example.android_practice.catalog.presentation.filters.FilterBadgeCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class CatalogFiltersPersistenceTest {
    @Test
    fun filtersAndBadgeSurviveReopeningAndResetRestoresDefaults() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val file = File(context.cacheDir, "filters-test-${UUID.randomUUID()}.preferences_pb")
        val firstJob = Job()
        val secondJob = Job()
        try {
            val firstStore = PreferenceDataStoreFactory.create(scope = CoroutineScope(firstJob + Dispatchers.IO)) { file }
            DataStoreCatalogFiltersRepository(firstStore).save(CatalogFilters(" PIKA ", " ELECTRIC "))
            firstJob.cancelAndJoin()

            val scope = CoroutineScope(secondJob + Dispatchers.IO)
            val reopenedStore = PreferenceDataStoreFactory.create(scope = scope) { file }
            val repository = DataStoreCatalogFiltersRepository(reopenedStore)
            assertEquals(CatalogFilters("pika", "electric"), repository.filters.first())
            val badge = FilterBadgeCache(repository, scope)
            assertTrue(withTimeout(5_000) { badge.isActive.first { it } })
            repository.reset()
            assertEquals(CatalogFilters(), repository.filters.first())
            assertFalse(withTimeout(5_000) { badge.isActive.first { !it } })
        } finally {
            firstJob.cancelAndJoin()
            secondJob.cancelAndJoin()
            file.delete()
        }
    }
}
