package com.example.android_practice.catalog.domain.usecase

import com.example.android_practice.catalog.domain.model.PokemonPage
import com.example.android_practice.catalog.domain.repository.PokemonRepository

class GetPokemonPage(private val repository: PokemonRepository) {
    suspend operator fun invoke(
        limit: Int = 20,
        offset: Int = 0,
        refresh: Boolean = false,
    ): PokemonPage = repository.getPokemonPage(limit, offset, refresh)
}
