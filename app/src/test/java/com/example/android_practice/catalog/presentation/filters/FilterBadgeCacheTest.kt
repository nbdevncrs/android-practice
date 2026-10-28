package com.example.android_practice.catalog.presentation.filters

import com.example.android_practice.catalog.domain.model.CatalogFilters
import com.example.android_practice.catalog.domain.repository.CatalogFiltersRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FilterBadgeCacheTest {
    @Test
    fun restoresBadgeFromSavedFiltersAndUpdatesAfterReset() = runTest {
        val repository = FakeFiltersRepository(CatalogFilters(type = "fire"))
        val cache = FilterBadgeCache(repository, backgroundScope)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            cache.isActive.collect { }
        }
        runCurrent()
        assertTrue(cache.isActive.value)

        repository.reset()
        runCurrent()
        assertFalse(cache.isActive.value)

        repository.save(CatalogFilters(nameQuery = "pika"))
        runCurrent()
        assertTrue(cache.isActive.value)
    }

    private class FakeFiltersRepository(initial: CatalogFilters) : CatalogFiltersRepository {
        override val filters = MutableStateFlow(initial)

        override suspend fun save(filters: CatalogFilters) {
            this.filters.value = filters.normalized()
        }

        override suspend fun reset() = save(CatalogFilters())
    }
}
