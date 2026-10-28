package com.example.android_practice.catalog.presentation

import com.example.android_practice.catalog.presentation.list.CatalogViewModel
import com.example.android_practice.catalog.presentation.list.CatalogUiState

import androidx.lifecycle.ViewModelStore
import com.example.android_practice.catalog.domain.model.CatalogError
import com.example.android_practice.catalog.domain.model.CatalogFilters
import com.example.android_practice.catalog.domain.model.CatalogException
import com.example.android_practice.catalog.domain.model.PokemonPage
import com.example.android_practice.catalog.domain.usecase.GetPokemonPage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private val store = ViewModelStore()

    @After
    fun tearDown() {
        store.clear()
    }

    @Test
    fun loadingRemainsVisibleUntilDataArrives() = runTest {
        val response = CompletableDeferred<PokemonPage>()
        val repository = TestPokemonRepository().apply { pageHandler = { _, _, _ -> response.await() } }
        val vm = createViewModel(repository)
        runCurrent()
        assertEquals(CatalogUiState.Loading, vm.uiState.value)

        response.complete(page())
        runCurrent()
        assertEquals(CatalogUiState.Content(page()), vm.uiState.value)
    }

    @Test
    fun retryRestoresContentUsingTheFailedPageParameters() = runTest {
        val repository = TestPokemonRepository().apply {
            pageHandler = { _, _, _ -> throw CatalogException(CatalogError.NETWORK, IOException("Offline")) }
        }
        val vm = createViewModel(repository)
        vm.loadPage(limit = 10, offset = 40)
        runCurrent()
        assertEquals(CatalogUiState.Error(CatalogError.NETWORK), vm.uiState.value)

        repository.pageHandler = { _, _, _ -> page() }
        vm.retry()
        assertEquals(CatalogUiState.Loading, vm.uiState.value)
        runCurrent()
        assertEquals(CatalogUiState.Content(page()), vm.uiState.value)
        assertEquals(listOf(Triple(10, 40, false), Triple(10, 40, false)), repository.pageRequests)
    }

    @Test
    fun refreshAndItsRetryBothBypassCache() = runTest {
        val repository = TestPokemonRepository()
        val vm = createViewModel(repository)
        runCurrent()
        repository.pageHandler = { _, _, _ -> throw CatalogException(CatalogError.NETWORK, IOException("Offline")) }
        vm.refresh()
        runCurrent()
        assertEquals(CatalogUiState.Error(CatalogError.NETWORK), vm.uiState.value)

        repository.pageHandler = { _, _, _ -> page("updated") }
        vm.retry()
        runCurrent()
        assertEquals(CatalogUiState.Content(page("updated")), vm.uiState.value)
        assertEquals(listOf(false, true, true), repository.pageRequests.map { it.third })
    }

    @Test
    fun cancelledRequestCannotOverwriteNewerContent() = runTest {
        val repository = TestPokemonRepository().apply {
            pageHandler = { _, offset, _ ->
                if (offset == 0) withContext(NonCancellable) {
                    delay(100)
                    page("old")
                } else page("new")
            }
        }
        val vm = createViewModel(repository)
        runCurrent()
        vm.loadPage(offset = 20)
        runCurrent()
        assertEquals(CatalogUiState.Content(page("new")), vm.uiState.value)

        advanceUntilIdle()
        assertEquals(CatalogUiState.Content(page("new")), vm.uiState.value)
    }

    @Test
    fun clearingViewModelCancelsLoadingWithoutDisplayingError() = runTest {
        var cancelled = false
        val repository = TestPokemonRepository().apply {
            pageHandler = { _, _, _ ->
                try { awaitCancellation() } finally { cancelled = true }
            }
        }
        val vm = createViewModel(repository)
        runCurrent()
        store.clear()
        runCurrent()

        assertTrue(cancelled)
        assertEquals(CatalogUiState.Loading, vm.uiState.value)
    }

    private fun createViewModel(repository: TestPokemonRepository) =
        CatalogViewModel(GetPokemonPage(repository), TestCatalogFiltersRepository()).also { store.put("catalog", it) }

    @Test
    fun refreshAndRetryKeepAppliedFilters() = runTest {
        val repository = TestPokemonRepository()
        val vm = createViewModel(repository)
        vm.loadPage(filters = CatalogFilters(" PIKA ", " ELECTRIC "))
        runCurrent()
        vm.refresh()
        runCurrent()
        vm.retry()
        runCurrent()

        assertEquals(List(3) { CatalogFilters("pika", "electric") }, repository.pageFilters)
        assertEquals(listOf(false, true, true), repository.pageRequests.map { it.third })
    }

    @Test
    fun initialRequestUsesSavedFiltersAndReloadReadsTheirNewValues() = runTest {
        val repository = TestPokemonRepository()
        val settings = TestCatalogFiltersRepository(CatalogFilters(type = "water"))
        val vm = CatalogViewModel(GetPokemonPage(repository), settings).also { store.put("catalog", it) }
        assertTrue(repository.pageRequests.isEmpty())
        runCurrent()
        assertEquals(listOf(CatalogFilters(type = "water")), repository.pageFilters)

        settings.save(CatalogFilters("pika", "electric"))
        vm.reloadFilters()
        runCurrent()
        assertEquals(listOf(CatalogFilters(type = "water"), CatalogFilters("pika", "electric")), repository.pageFilters)
    }

    @Test
    fun settingsReadFailureDoesNotLoadDefaultCatalogAndRetryRestoresSavedFilters() = runTest {
        val repository = TestPokemonRepository()
        val settings = TestCatalogFiltersRepository(CatalogFilters(type = "fire")).apply {
            readError = IOException("Storage unavailable")
        }
        val vm = CatalogViewModel(GetPokemonPage(repository), settings).also { store.put("catalog", it) }
        runCurrent()
        assertEquals(CatalogUiState.Error(CatalogError.STORAGE), vm.uiState.value)
        assertTrue(repository.pageRequests.isEmpty())

        settings.readError = null
        vm.retry()
        runCurrent()
        assertEquals(listOf(CatalogFilters(type = "fire")), repository.pageFilters)
        assertTrue(vm.uiState.value is CatalogUiState.Content)
    }
}
