package com.example.android_practice.catalog.data.mapper

import com.example.android_practice.catalog.data.remote.dto.NamedResourceDto
import com.example.android_practice.catalog.data.remote.dto.PokemonDetailsDto
import com.example.android_practice.catalog.domain.model.PokemonAbility
import com.example.android_practice.catalog.domain.model.PokemonDetails
import com.example.android_practice.catalog.domain.model.PokemonStats
import com.example.android_practice.catalog.domain.model.PokemonSummary

internal class InvalidPokemonDataException(message: String) : Exception(message)

internal fun NamedResourceDto.pokemonId(): Int {
    val id = url.trimEnd('/').substringAfterLast('/').toIntOrNull()
    if (id == null || id <= 0) {
        throw InvalidPokemonDataException("Invalid Pokémon URL: $url")
    }
    return id
}

internal fun PokemonDetailsDto.toDomain(): PokemonDetails {
    val values = stats.associate { it.stat.name to it.baseStat }
    fun stat(name: String): Int = values[name]
        ?: throw InvalidPokemonDataException("Missing Pokémon stat: $name")

    return PokemonDetails(
        id = id,
        name = name,
        types = types.sortedBy { it.slot }.map { it.type.name },
        imageUrl = sprites.other?.officialArtwork?.frontDefault ?: sprites.frontDefault,
        heightDecimeters = height,
        weightHectograms = weight,
        abilities = abilities.sortedBy { it.slot }.map { PokemonAbility(it.ability.name, it.isHidden) },
        stats = PokemonStats(
            hp = stat("hp"),
            attack = stat("attack"),
            defense = stat("defense"),
            specialAttack = stat("special-attack"),
            specialDefense = stat("special-defense"),
            speed = stat("speed"),
        ),
    )
}

internal fun PokemonDetails.toSummary(): PokemonSummary = PokemonSummary(id, name, types, imageUrl)
