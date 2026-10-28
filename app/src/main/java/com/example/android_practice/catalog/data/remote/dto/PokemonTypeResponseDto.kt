package com.example.android_practice.catalog.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PokemonTypeResponseDto(val pokemon: List<PokemonTypeMemberDto>)

@Serializable
data class PokemonTypeMemberDto(val pokemon: NamedResourceDto)
