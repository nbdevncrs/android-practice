package com.example.android_practice.catalog.presentation

import com.example.android_practice.catalog.domain.model.PokemonAbility
import com.example.android_practice.catalog.domain.model.PokemonDetails
import com.example.android_practice.catalog.domain.model.PokemonPage
import com.example.android_practice.catalog.domain.model.PokemonStats
import com.example.android_practice.catalog.domain.model.PokemonSummary
import com.example.android_practice.catalog.domain.repository.PokemonRepository

internal class TestPokemonRepository : PokemonRepository {
    val pageRequests = mutableListOf<Triple<Int, Int, Boolean>>()
    val detailsRequests = mutableListOf<Pair<Int, Boolean>>()
    var pageHandler: suspend (Int, Int, Boolean) -> PokemonPage = { _, _, _ -> page() }
    var detailsHandler: suspend (Int, Boolean) -> PokemonDetails = { id, _ -> details(id) }

    override suspend fun getPokemonPage(limit: Int, offset: Int, refresh: Boolean): PokemonPage {
        pageRequests += Triple(limit, offset, refresh)
        return pageHandler(limit, offset, refresh)
    }

    override suspend fun getPokemonDetails(id: Int, refresh: Boolean): PokemonDetails {
        detailsRequests += id to refresh
        return detailsHandler(id, refresh)
    }
}

internal fun page(name: String = "bulbasaur") = PokemonPage(
    items = listOf(PokemonSummary(1, name, listOf("grass", "poison"), null)),
    totalCount = 1,
    hasNextPage = false,
)

internal fun details(id: Int = 1, name: String = "bulbasaur") = PokemonDetails(
    id = id,
    name = name,
    types = listOf("grass", "poison"),
    imageUrl = null,
    heightDecimeters = 7,
    weightHectograms = 69,
    abilities = listOf(PokemonAbility("overgrow", false)),
    stats = PokemonStats(45, 49, 49, 65, 65, 45),
)
