package com.example.android_practice.catalog.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PokemonPageDto(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<NamedResourceDto>,
)

@Serializable
data class NamedResourceDto(
    val name: String,
    val url: String,
)
