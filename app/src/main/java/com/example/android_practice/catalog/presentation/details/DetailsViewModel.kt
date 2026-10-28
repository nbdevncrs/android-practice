package com.example.android_practice.catalog.presentation.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.android_practice.catalog.domain.model.CatalogException
import com.example.android_practice.catalog.domain.model.CatalogError
import com.example.android_practice.favorites.domain.repository.FavoritesRepository
import com.example.android_practice.favorites.domain.model.FavoritesStorageException
import com.example.android_practice.catalog.domain.usecase.GetPokemonDetails
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetailsViewModel(
    private val getPokemonDetails: GetPokemonDetails,
    savedStateHandle: SavedStateHandle,
    private val favoritesRepository: FavoritesRepository,
) : ViewModel() {
    private val pokemonId = requireNotNull(savedStateHandle.get<Int>(POKEMON_ID_KEY)) {
        "Missing Pokémon ID"
    }.also { require(it > 0) }

    private val _uiState = MutableStateFlow<DetailsUiState>(DetailsUiState.Loading)
    val uiState = _uiState.asStateFlow()
    private val _favoriteState = MutableStateFlow(FavoriteActionState())
    val favoriteState = _favoriteState.asStateFlow()
    private val savedOnly = savedStateHandle.get<Boolean>(SAVED_ONLY_KEY) ?: false
    private var favoriteStatusJob: Job? = null

    private var loadJob: Job? = null
    private var lastRefresh = false

    init {
        observeFavoriteStatus()
        load(refresh = false)
    }

    private fun observeFavoriteStatus() {
        favoriteStatusJob?.cancel()
        _favoriteState.update { it.copy(hasError = false) }
        favoriteStatusJob = viewModelScope.launch {
            try {
                favoritesRepository.observeIsFavorite(pokemonId).collect { value ->
                    _favoriteState.update { it.copy(isFavorite = value, hasError = false) }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: FavoritesStorageException) {
                _favoriteState.update { it.copy(hasError = true) }
            }
        }
    }

    fun toggleFavorite() {
        val action = _favoriteState.value
        if (action.isChanging) return
        if (action.isFavorite == null) {
            observeFavoriteStatus()
            return
        }
        val pokemon = (_uiState.value as? DetailsUiState.Content)?.pokemon ?: return
        _favoriteState.update { it.copy(isChanging = true, hasError = false) }
        viewModelScope.launch {
            try {
                if (action.isFavorite) favoritesRepository.remove(pokemonId) else favoritesRepository.save(pokemon)
                val saved = if (action.isFavorite) null else favoritesRepository.getPokemonDetails(pokemonId)
                _uiState.update { state ->
                    if (state is DetailsUiState.Content) {
                        state.copy(pokemon = state.pokemon.copy(localImagePath = saved?.localImagePath))
                    } else state
                }
                _favoriteState.update { it.copy(isFavorite = !action.isFavorite, isChanging = false) }
            } catch (error: CancellationException) {
                throw error
            } catch (_: FavoritesStorageException) {
                _favoriteState.update { it.copy(isChanging = false, hasError = true) }
            }
        }
    }

    fun retry() = load(lastRefresh)

    fun refresh() = load(refresh = true)

    private fun load(refresh: Boolean) {
        lastRefresh = refresh
        loadJob?.cancel()
        _uiState.value = DetailsUiState.Loading
        loadJob = viewModelScope.launch {
            try {
                val pokemon = if (savedOnly || !refresh) {
                    val saved = favoritesRepository.getPokemonDetails(pokemonId)
                    saved ?: if (savedOnly) {
                        throw CatalogException(CatalogError.NOT_FOUND, IllegalStateException("Favorite no longer exists"))
                    } else getPokemonDetails(pokemonId, refresh)
                } else getPokemonDetails(pokemonId, refresh)
                currentCoroutineContext().ensureActive()
                _uiState.value = DetailsUiState.Content(pokemon)
            } catch (error: CancellationException) {
                throw error
            } catch (error: CatalogException) {
                currentCoroutineContext().ensureActive()
                _uiState.value = DetailsUiState.Error(error.error)
            } catch (_: FavoritesStorageException) {
                currentCoroutineContext().ensureActive()
                _uiState.value = DetailsUiState.Error(CatalogError.STORAGE)
            }
        }
    }

    companion object {
        const val POKEMON_ID_KEY = "pokemonId"
        const val SAVED_ONLY_KEY = "savedOnly"

        fun factory(getPokemonDetails: GetPokemonDetails, favoritesRepository: FavoritesRepository) = viewModelFactory {
            initializer { DetailsViewModel(getPokemonDetails, createSavedStateHandle(), favoritesRepository) }
        }
    }
}
