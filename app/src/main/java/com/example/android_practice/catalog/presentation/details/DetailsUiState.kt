package com.example.android_practice.catalog.presentation.details

import com.example.android_practice.catalog.domain.model.CatalogError
import com.example.android_practice.catalog.domain.model.PokemonDetails

sealed interface DetailsUiState {
    data object Loading : DetailsUiState
    data class Content(val pokemon: PokemonDetails) : DetailsUiState
    data class Error(val error: CatalogError) : DetailsUiState
}
