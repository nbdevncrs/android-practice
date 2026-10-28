package com.example.android_practice.catalog.presentation.list

import com.example.android_practice.catalog.domain.model.CatalogError
import com.example.android_practice.catalog.domain.model.PokemonPage

sealed interface CatalogUiState {
    data object Loading : CatalogUiState
    data class Content(val page: PokemonPage) : CatalogUiState
    data class Error(val error: CatalogError) : CatalogUiState
}
