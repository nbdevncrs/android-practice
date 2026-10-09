package com.example.android_practice.catalog.domain.model

data class PokemonPage(
    val items: List<PokemonSummary>,
    val totalCount: Int,
    val hasNextPage: Boolean,
)
