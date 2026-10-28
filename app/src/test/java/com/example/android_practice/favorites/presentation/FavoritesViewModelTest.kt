package com.example.android_practice.favorites.presentation

import androidx.lifecycle.ViewModelStore
import com.example.android_practice.catalog.presentation.MainDispatcherRule
import com.example.android_practice.catalog.presentation.details
import com.example.android_practice.favorites.TestFavoritesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @Test
    fun listUpdatesWhenFavoritesAreAddedAndRemoved() = runTest {
        val repository = TestFavoritesRepository()
        val store = ViewModelStore()
        val vm = FavoritesViewModel(repository).also { store.put("favorites", it) }
        try {
            runCurrent()
            assertEquals(FavoritesUiState.Content(emptyList()), vm.uiState.value)
            repository.save(details(25))
            runCurrent()
            assertEquals(listOf(25), (vm.uiState.value as FavoritesUiState.Content).pokemon.map { it.id })
            repository.remove(25)
            runCurrent()
            assertEquals(FavoritesUiState.Content(emptyList()), vm.uiState.value)
        } finally {
            store.clear()
        }
    }
}
