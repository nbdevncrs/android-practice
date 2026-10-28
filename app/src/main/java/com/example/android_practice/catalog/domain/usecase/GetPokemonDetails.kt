package com.example.android_practice.catalog.domain.usecase

import com.example.android_practice.catalog.domain.model.PokemonDetails
import com.example.android_practice.catalog.domain.repository.PokemonRepository

class GetPokemonDetails(private val repository: PokemonRepository) {
    suspend operator fun invoke(id: Int, refresh: Boolean = false): PokemonDetails =
        repository.getPokemonDetails(id, refresh)
}
