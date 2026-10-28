package com.example.android_practice.catalog.presentation.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.android_practice.catalog.domain.model.CatalogException
import com.example.android_practice.catalog.domain.usecase.GetPokemonDetails
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetailsViewModel(
    private val getPokemonDetails: GetPokemonDetails,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val pokemonId = requireNotNull(savedStateHandle.get<Int>(POKEMON_ID_KEY)) {
        "Missing Pokémon ID"
    }.also { require(it > 0) }

    private val _uiState = MutableStateFlow<DetailsUiState>(DetailsUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var lastRefresh = false

    init {
        load(refresh = false)
    }

    fun retry() = load(lastRefresh)

    fun refresh() = load(refresh = true)

    private fun load(refresh: Boolean) {
        lastRefresh = refresh
        loadJob?.cancel()
        _uiState.value = DetailsUiState.Loading
        loadJob = viewModelScope.launch {
            try {
                val pokemon = getPokemonDetails(pokemonId, refresh)
                currentCoroutineContext().ensureActive()
                _uiState.value = DetailsUiState.Content(pokemon)
            } catch (error: CancellationException) {
                throw error
            } catch (error: CatalogException) {
                currentCoroutineContext().ensureActive()
                _uiState.value = DetailsUiState.Error(error.error)
            }
        }
    }

    companion object {
        const val POKEMON_ID_KEY = "pokemonId"

        fun factory(getPokemonDetails: GetPokemonDetails) = viewModelFactory {
            initializer { DetailsViewModel(getPokemonDetails, createSavedStateHandle()) }
        }
    }
}
