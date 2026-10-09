package com.example.android_practice.catalog.domain.repository

import com.example.android_practice.catalog.domain.model.PokemonDetails
import com.example.android_practice.catalog.domain.model.PokemonPage

interface PokemonRepository {
    suspend fun getPokemonPage(limit: Int, offset: Int, refresh: Boolean = false): PokemonPage

    suspend fun getPokemonDetails(id: Int, refresh: Boolean = false): PokemonDetails
}
