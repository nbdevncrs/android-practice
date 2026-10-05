package com.example.android_practice.catalog.domain

data class PokemonSummary(
    val id: Int,
    val name: String,
    val types: List<PokemonType>,
    val imageUrl: String?,
)

data class PokemonDetails(
    val id: Int,
    val name: String,
    val types: List<PokemonType>,
    val imageUrl: String?,
    val heightDecimeters: Int,
    val weightHectograms: Int,
    val abilities: List<String>,
    val stats: PokemonStats,
)

data class PokemonStats(
    val hp: Int,
    val attack: Int,
    val defense: Int,
    val specialAttack: Int,
    val specialDefense: Int,
    val speed: Int,
)

enum class PokemonType {
    NORMAL, FIRE, WATER, ELECTRIC, GRASS, ICE, FIGHTING, POISON, GROUND,
    FLYING, PSYCHIC, BUG, ROCK, GHOST, DRAGON, DARK, STEEL, FAIRY,
}
