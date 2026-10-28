package com.example.android_practice.catalog.domain.model

data class PokemonDetails(
    val id: Int,
    val name: String,
    val types: List<String>,
    val imageUrl: String?,
    val heightDecimeters: Int,
    val weightHectograms: Int,
    val abilities: List<PokemonAbility>,
    val stats: PokemonStats,
    val localImagePath: String? = null,
)
