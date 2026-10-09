package com.example.android_practice.catalog.data

import com.example.android_practice.catalog.domain.PokemonDetails
import com.example.android_practice.catalog.domain.PokemonRepository
import com.example.android_practice.catalog.domain.PokemonStats
import com.example.android_practice.catalog.domain.PokemonSummary
import com.example.android_practice.catalog.domain.PokemonType

class MockPokemonRepository : PokemonRepository {
    override suspend fun getPokemon(): List<PokemonSummary> = pokemon.map { details ->
        PokemonSummary(details.id, details.name, details.types, details.imageUrl)
    }

    override suspend fun getPokemonDetails(id: Int): PokemonDetails? =
        pokemon.firstOrNull { it.id == id }

    private companion object {
        val pokemon = listOf(
            PokemonDetails(1, "Bulbasaur", listOf(PokemonType.GRASS, PokemonType.POISON), null, 7, 69,
                listOf("Overgrow", "Chlorophyll"), PokemonStats(45, 49, 49, 65, 65, 45)),
            PokemonDetails(2, "Ivysaur", listOf(PokemonType.GRASS, PokemonType.POISON), null, 10, 130,
                listOf("Overgrow", "Chlorophyll"), PokemonStats(60, 62, 63, 80, 80, 60)),
            PokemonDetails(3, "Venusaur", listOf(PokemonType.GRASS, PokemonType.POISON), null, 20, 1000,
                listOf("Overgrow", "Chlorophyll"), PokemonStats(80, 82, 83, 100, 100, 80)),
            PokemonDetails(4, "Charmander", listOf(PokemonType.FIRE), null, 6, 85,
                listOf("Blaze", "Solar Power"), PokemonStats(39, 52, 43, 60, 50, 65)),
            PokemonDetails(5, "Charmeleon", listOf(PokemonType.FIRE), null, 11, 190,
                listOf("Blaze", "Solar Power"), PokemonStats(58, 64, 58, 80, 65, 80)),
            PokemonDetails(6, "Charizard", listOf(PokemonType.FIRE, PokemonType.FLYING), null, 17, 905,
                listOf("Blaze", "Solar Power"), PokemonStats(78, 84, 78, 109, 85, 100)),
            PokemonDetails(7, "Squirtle", listOf(PokemonType.WATER), null, 5, 90,
                listOf("Torrent", "Rain Dish"), PokemonStats(44, 48, 65, 50, 64, 43)),
            PokemonDetails(8, "Wartortle", listOf(PokemonType.WATER), null, 10, 225,
                listOf("Torrent", "Rain Dish"), PokemonStats(59, 63, 80, 65, 80, 58)),
            PokemonDetails(9, "Blastoise", listOf(PokemonType.WATER), null, 16, 855,
                listOf("Torrent", "Rain Dish"), PokemonStats(79, 83, 100, 85, 105, 78)),
            PokemonDetails(10, "Caterpie", listOf(PokemonType.BUG), null, 3, 29,
                listOf("Shield Dust", "Run Away"), PokemonStats(45, 30, 35, 20, 20, 45)),
            PokemonDetails(25, "Pikachu", listOf(PokemonType.ELECTRIC), null, 4, 60,
                listOf("Static", "Lightning Rod"), PokemonStats(35, 55, 40, 50, 50, 90)),
            PokemonDetails(133, "Eevee", listOf(PokemonType.NORMAL), null, 3, 65,
                listOf("Run Away", "Adaptability", "Anticipation"), PokemonStats(55, 55, 50, 45, 65, 55)),
        )
    }
}
