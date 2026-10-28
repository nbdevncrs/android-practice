package com.example.android_practice.catalog.presentation.filters

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import com.example.android_practice.catalog.domain.model.CatalogFilters
import com.example.android_practice.catalog.presentation.MainDispatcherRule
import com.example.android_practice.catalog.presentation.TestCatalogFiltersRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class FiltersViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()
    private val store = ViewModelStore()

    @After
    fun tearDown() = store.clear()

    @Test
    fun draftAndResetDoNotChangeSavedFiltersUntilApply() = runTest {
        val initial = CatalogFilters("pika", "electric")
        val repository = TestCatalogFiltersRepository(initial)
        val vm = FiltersViewModel(repository, SavedStateHandle(), FilterBadgeCache(repository, backgroundScope))
            .also { store.put("filters", it) }
        runCurrent()
        assertEquals(initial, vm.uiState.value.draft)

        vm.setNameQuery("char")
        vm.setType("fire")
        assertEquals(initial, repository.currentFilters)
        vm.reset()
        assertEquals(CatalogFilters(), vm.uiState.value.draft)
        assertEquals(initial, repository.currentFilters)

        vm.apply()
        runCurrent()
        assertEquals(CatalogFilters(), repository.currentFilters)
        assertTrue(vm.uiState.value.isSaved)
    }

    @Test
    fun saveFailurePreservesDraftAndAllowsRetry() = runTest {
        val repository = TestCatalogFiltersRepository()
        val vm = FiltersViewModel(repository, SavedStateHandle(), FilterBadgeCache(repository, backgroundScope))
            .also { store.put("filters", it) }
        runCurrent()
        vm.setNameQuery(" PIKA ")
        repository.saveError = IOException("Disk unavailable")
        vm.apply()
        runCurrent()
        assertTrue(vm.uiState.value.hasError)
        assertFalse(vm.uiState.value.isSaved)
        assertEquals(" PIKA ", vm.uiState.value.draft.nameQuery)
        assertEquals(CatalogFilters(), repository.currentFilters)

        repository.saveError = null
        vm.apply()
        runCurrent()
        assertTrue(vm.uiState.value.isSaved)
        assertEquals(CatalogFilters("pika"), repository.currentFilters)
    }

    @Test
    fun draftSurvivesViewModelRecreation() = runTest {
        val repository = TestCatalogFiltersRepository(CatalogFilters(type = "water"))
        val handle = SavedStateHandle()
        val cache = FilterBadgeCache(repository, backgroundScope)
        val first = FiltersViewModel(repository, handle, cache).also { store.put("first", it) }
        runCurrent()
        first.setNameQuery("squirt")
        val restoredHandle = SavedStateHandle(handle.keys().associateWith { handle.get<Any?>(it) })
        store.clear()

        val restored = FiltersViewModel(repository, restoredHandle, cache).also { store.put("restored", it) }
        runCurrent()
        assertEquals(CatalogFilters("squirt", "water"), restored.uiState.value.draft)
        assertEquals(CatalogFilters(type = "water"), repository.currentFilters)
    }

    @Test
    fun readFailureBlocksApplyUntilSettingsHaveBeenRestored() = runTest {
        val repository = TestCatalogFiltersRepository(CatalogFilters(type = "water")).apply {
            readError = IOException("Storage unavailable")
        }
        val vm = FiltersViewModel(repository, SavedStateHandle(), FilterBadgeCache(repository, backgroundScope))
            .also { store.put("filters", it) }
        runCurrent()
        assertTrue(vm.uiState.value.hasLoadError)
        vm.reset()
        vm.apply()
        runCurrent()
        assertEquals(CatalogFilters(type = "water"), repository.currentFilters)
        assertFalse(vm.uiState.value.isSaved)

        repository.readError = null
        vm.retryLoading()
        runCurrent()
        assertFalse(vm.uiState.value.hasLoadError)
        assertEquals(CatalogFilters(type = "water"), vm.uiState.value.draft)
    }
}
