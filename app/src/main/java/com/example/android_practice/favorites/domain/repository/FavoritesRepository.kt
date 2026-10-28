package com.example.android_practice.favorites.domain.repository

import com.example.android_practice.catalog.domain.model.PokemonDetails
import com.example.android_practice.catalog.domain.model.PokemonSummary
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    fun observeFavorites(): Flow<List<PokemonSummary>>

    fun observeIsFavorite(id: Int): Flow<Boolean>

    suspend fun getPokemonDetails(id: Int): PokemonDetails?

    suspend fun save(pokemon: PokemonDetails)

    suspend fun remove(id: Int)
}
