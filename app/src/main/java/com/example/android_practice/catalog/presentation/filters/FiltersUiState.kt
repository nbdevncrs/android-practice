package com.example.android_practice.catalog.presentation.filters

import com.example.android_practice.catalog.domain.model.CatalogFilters

data class FiltersUiState(
    val draft: CatalogFilters = CatalogFilters(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val hasError: Boolean = false,
    val hasLoadError: Boolean = false,
    val isSaved: Boolean = false,
)
