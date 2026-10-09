package com.example.android_practice.catalog.domain

interface PokemonRepository {
    suspend fun getPokemon(): List<PokemonSummary>

    suspend fun getPokemonDetails(id: Int): PokemonDetails?
}
