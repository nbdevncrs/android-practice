package com.example.android_practice.catalog.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PokemonDetailsDto(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val types: List<PokemonTypeDto>,
    val abilities: List<PokemonAbilityDto>,
    val stats: List<PokemonStatDto>,
    val sprites: PokemonSpritesDto,
)

@Serializable
data class PokemonTypeDto(
    val slot: Int,
    val type: NamedResourceDto,
)

@Serializable
data class PokemonAbilityDto(
    @SerialName("is_hidden") val isHidden: Boolean,
    val slot: Int,
    val ability: NamedResourceDto,
)

@Serializable
data class PokemonStatDto(
    @SerialName("base_stat") val baseStat: Int,
    val stat: NamedResourceDto,
)

@Serializable
data class PokemonSpritesDto(
    @SerialName("front_default") val frontDefault: String? = null,
    val other: PokemonOtherSpritesDto? = null,
)

@Serializable
data class PokemonOtherSpritesDto(
    @SerialName("official-artwork") val officialArtwork: PokemonArtworkDto? = null,
)

@Serializable
data class PokemonArtworkDto(
    @SerialName("front_default") val frontDefault: String? = null,
)
