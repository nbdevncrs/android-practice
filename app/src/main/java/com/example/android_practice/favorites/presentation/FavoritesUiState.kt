package com.example.android_practice.favorites.presentation

import com.example.android_practice.catalog.domain.model.PokemonSummary

sealed interface FavoritesUiState {
    data object Loading : FavoritesUiState
    data class Content(val pokemon: List<PokemonSummary>) : FavoritesUiState
    data object Error : FavoritesUiState
}
