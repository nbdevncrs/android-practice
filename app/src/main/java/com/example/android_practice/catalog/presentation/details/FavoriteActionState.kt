package com.example.android_practice.catalog.presentation.details

data class FavoriteActionState(
    val isFavorite: Boolean? = null,
    val isChanging: Boolean = false,
    val hasError: Boolean = false,
)
