package com.example.android_practice.catalog.data

import com.example.android_practice.catalog.data.remote.dto.NamedResourceDto
import com.example.android_practice.catalog.data.remote.dto.PokemonAbilityDto
import com.example.android_practice.catalog.data.remote.dto.PokemonArtworkDto
import com.example.android_practice.catalog.data.remote.dto.PokemonDetailsDto
import com.example.android_practice.catalog.data.remote.dto.PokemonOtherSpritesDto
import com.example.android_practice.catalog.data.remote.dto.PokemonSpritesDto
import com.example.android_practice.catalog.data.remote.dto.PokemonStatDto
import com.example.android_practice.catalog.data.remote.dto.PokemonTypeDto

internal fun pokemonDto(id: Int = 1): PokemonDetailsDto = PokemonDetailsDto(
    id = id,
    name = "pokemon-$id",
    height = 7,
    weight = 69,
    types = listOf(
        PokemonTypeDto(2, resource("poison")),
        PokemonTypeDto(1, resource("grass")),
    ),
    abilities = listOf(
        PokemonAbilityDto(true, 3, resource("chlorophyll")),
        PokemonAbilityDto(false, 1, resource("overgrow")),
    ),
    stats = listOf(
        "speed" to 45,
        "special-defense" to 65,
        "hp" to 45,
        "defense" to 49,
        "special-attack" to 65,
        "attack" to 49,
    ).map { (name, value) -> PokemonStatDto(value, resource(name)) },
    sprites = PokemonSpritesDto(
        frontDefault = "https://example.com/sprite.png",
        other = PokemonOtherSpritesDto(PokemonArtworkDto("https://example.com/artwork.png")),
    ),
)

private fun resource(name: String) = NamedResourceDto(name, "https://example.com/$name/")
