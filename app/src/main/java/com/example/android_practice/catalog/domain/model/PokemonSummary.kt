package com.example.android_practice.catalog.domain.model

data class PokemonSummary(
    val id: Int,
    val name: String,
    val types: List<String>,
    val imageUrl: String?,
    val localImagePath: String? = null,
)
