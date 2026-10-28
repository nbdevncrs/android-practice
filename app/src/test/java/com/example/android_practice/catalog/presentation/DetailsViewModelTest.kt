package com.example.android_practice.catalog.presentation

import com.example.android_practice.catalog.presentation.details.DetailsViewModel
import com.example.android_practice.catalog.presentation.details.DetailsUiState

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import com.example.android_practice.catalog.domain.model.CatalogError
import com.example.android_practice.catalog.domain.model.CatalogException
import com.example.android_practice.catalog.domain.model.PokemonDetails
import com.example.android_practice.catalog.domain.usecase.GetPokemonDetails
import com.example.android_practice.favorites.TestFavoritesRepository
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
        TestFavoritesRepository(),
    ).also { store.put("details", it) }

    @Test
    fun savedDetailsAndRefreshNeverRequestNetworkFromFavorites() = runTest {
        val network = TestPokemonRepository()
        val favorites = TestFavoritesRepository()
        val pokemon = details(25).copy(localImagePath = "/saved/25.img")
        favorites.save(pokemon)
        val vm = DetailsViewModel(GetPokemonDetails(network), SavedStateHandle(mapOf(
            DetailsViewModel.POKEMON_ID_KEY to 25, DetailsViewModel.SAVED_ONLY_KEY to true,
        )), favorites).also { store.put("details", it) }
        runCurrent()
        vm.refresh()
        runCurrent()

        assertEquals(DetailsUiState.Content(pokemon), vm.uiState.value)
        assertTrue(network.detailsRequests.isEmpty())
        assertEquals(true, vm.favoriteState.value.isFavorite)
    }

    @Test
    fun favoriteToggleSavesAndRemovesLoadedPokemon() = runTest {
        val favorites = TestFavoritesRepository()
        val vm = DetailsViewModel(GetPokemonDetails(TestPokemonRepository()),
            SavedStateHandle(mapOf(DetailsViewModel.POKEMON_ID_KEY to 1)), favorites)
            .also { store.put("details", it) }
        runCurrent()
        vm.toggleFavorite()
        runCurrent()
        assertEquals(details(1), favorites.getPokemonDetails(1))
        assertEquals(true, vm.favoriteState.value.isFavorite)
        vm.toggleFavorite()
        runCurrent()
        assertTrue(favorites.saved.value.isEmpty())
        assertEquals(false, vm.favoriteState.value.isFavorite)
    }

    @Test
    fun storageFailureKeepsContentAndAllowsFavoriteRetry() = runTest {
        val favorites = TestFavoritesRepository()
        val vm = DetailsViewModel(GetPokemonDetails(TestPokemonRepository()),
            SavedStateHandle(mapOf(DetailsViewModel.POKEMON_ID_KEY to 1)), favorites)
            .also { store.put("details", it) }
        runCurrent()
        favorites.writeError = IOException("Full disk")
        vm.toggleFavorite()
        runCurrent()
        assertTrue(vm.uiState.value is DetailsUiState.Content)
        assertTrue(vm.favoriteState.value.hasError)
        assertEquals(false, vm.favoriteState.value.isFavorite)

        favorites.writeError = null
        vm.toggleFavorite()
        runCurrent()
        assertFalse(vm.favoriteState.value.hasError)
        assertEquals(true, vm.favoriteState.value.isFavorite)
    }
}
