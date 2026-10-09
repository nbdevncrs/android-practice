package com.example.android_practice.catalog.presentation

import com.example.android_practice.catalog.presentation.details.DetailsViewModel
import com.example.android_practice.catalog.presentation.details.DetailsUiState

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import com.example.android_practice.catalog.domain.model.CatalogError
import com.example.android_practice.catalog.domain.model.CatalogException
import com.example.android_practice.catalog.domain.model.PokemonDetails
import com.example.android_practice.catalog.domain.usecase.GetPokemonDetails
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class DetailsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private val store = ViewModelStore()

    @After
    fun tearDown() {
        store.clear()
    }

    @Test
    fun loadsPokemonFromSavedNavigationId() = runTest {
        val response = CompletableDeferred<PokemonDetails>()
        val repository = TestPokemonRepository().apply { detailsHandler = { _, _ -> response.await() } }
        val vm = createViewModel(repository, id = 25)
        runCurrent()
        assertEquals(DetailsUiState.Loading, vm.uiState.value)
        assertEquals(listOf(25 to false), repository.detailsRequests)

        response.complete(details(25, "pikachu"))
        runCurrent()
        assertEquals(DetailsUiState.Content(details(25, "pikachu")), vm.uiState.value)
    }

    @Test
    fun retryAfterNotFoundRequestsTheSamePokemon() = runTest {
        val repository = TestPokemonRepository().apply {
            detailsHandler = { _, _ -> throw CatalogException(CatalogError.NOT_FOUND, IOException("Missing")) }
        }
        val vm = createViewModel(repository, id = 25)
        runCurrent()
        assertEquals(DetailsUiState.Error(CatalogError.NOT_FOUND), vm.uiState.value)

        repository.detailsHandler = { id, _ -> details(id) }
        vm.retry()
        runCurrent()
        assertEquals(DetailsUiState.Content(details(25)), vm.uiState.value)
        assertEquals(listOf(25 to false, 25 to false), repository.detailsRequests)
    }

    @Test
    fun retryAfterFailedRefreshKeepsBypassingCache() = runTest {
        val repository = TestPokemonRepository()
        val vm = createViewModel(repository)
        runCurrent()
        repository.detailsHandler = { _, _ -> throw CatalogException(CatalogError.NETWORK, IOException("Offline")) }
        vm.refresh()
        runCurrent()
        assertEquals(DetailsUiState.Error(CatalogError.NETWORK), vm.uiState.value)

        repository.detailsHandler = { id, _ -> details(id, "updated") }
        vm.retry()
        runCurrent()
        assertEquals(DetailsUiState.Content(details(1, "updated")), vm.uiState.value)
        assertEquals(listOf(1 to false, 1 to true, 1 to true), repository.detailsRequests)
    }

    @Test
    fun refreshCancelsInitialLoadAndIgnoresItsLateResult() = runTest {
        val repository = TestPokemonRepository().apply {
            detailsHandler = { id, refresh ->
                if (!refresh) withContext(NonCancellable) {
                    delay(100)
                    details(id, "old")
                } else details(id, "new")
            }
        }
        val vm = createViewModel(repository)
        runCurrent()
        vm.refresh()
        runCurrent()
        assertEquals(DetailsUiState.Content(details(1, "new")), vm.uiState.value)

        advanceUntilIdle()
        assertEquals(DetailsUiState.Content(details(1, "new")), vm.uiState.value)
    }

    private fun createViewModel(repository: TestPokemonRepository, id: Int = 1) = DetailsViewModel(
        GetPokemonDetails(repository),
        SavedStateHandle(mapOf(DetailsViewModel.POKEMON_ID_KEY to id)),
    ).also { store.put("details", it) }
}
