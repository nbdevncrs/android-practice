package com.example.android_practice.catalog.data.repository

import com.example.android_practice.catalog.data.pokemonDto
import com.example.android_practice.catalog.data.remote.PokemonApi
import com.example.android_practice.catalog.data.remote.dto.NamedResourceDto
import com.example.android_practice.catalog.data.remote.dto.PokemonPageDto
import com.example.android_practice.catalog.data.remote.dto.PokemonTypeResponseDto
import com.example.android_practice.catalog.data.remote.dto.PokemonTypeMemberDto
import com.example.android_practice.catalog.domain.model.CatalogFilters
import com.example.android_practice.catalog.domain.model.CatalogException
import com.example.android_practice.catalog.domain.model.CatalogError
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException

class PokemonFilteringTest {
    @Test
    fun nameSearchReadsWholeIndexAndLoadsOnlyRequestedMatches() = runTest {
        val api = FilteringApi()
        val repository = PokemonRepositoryImpl(api, StandardTestDispatcher(testScheduler))

        val page = repository.getPokemonPage(1, 1, filters = CatalogFilters(" PIKA "))

        assertEquals(listOf(26), page.items.map { it.id })
        assertEquals(3, page.totalCount)
        assertTrue(page.hasNextPage)
        assertEquals(listOf(500 to 0, 500 to 2), api.pageCalls)
        assertEquals(listOf(26), api.detailCalls)
    }

    @Test
    fun nameAndTypeAreCombinedAndTypeIndexIsReused() = runTest {
        val api = FilteringApi()
        val repository = PokemonRepositoryImpl(api, StandardTestDispatcher(testScheduler))

        val first = repository.getPokemonPage(20, 0, filters = CatalogFilters("PIKA", " ELECTRIC "))
        val second = repository.getPokemonPage(20, 0, filters = CatalogFilters(type = "electric"))

        assertEquals(listOf(25, 26), first.items.map { it.id })
        assertEquals(2, first.totalCount)
        assertFalse(first.hasNextPage)
        assertEquals(listOf(25, 26, 81), second.items.map { it.id })
        assertEquals(listOf("electric"), api.typeCalls)
        assertTrue(api.pageCalls.isEmpty())
        assertEquals(listOf(25, 26, 81), api.detailCalls)
    }

    @Test
    fun emptyResultsAndOffsetPastEndDoNotLoadDetails() = runTest {
        val api = FilteringApi()
        val repository = PokemonRepositoryImpl(api, StandardTestDispatcher(testScheduler))

        val empty = repository.getPokemonPage(20, 0, filters = CatalogFilters("missing"))
        val pastEnd = repository.getPokemonPage(20, Int.MAX_VALUE, filters = CatalogFilters("pika"))

        assertTrue(empty.items.isEmpty())
        assertEquals(0, empty.totalCount)
        assertFalse(empty.hasNextPage)
        assertTrue(pastEnd.items.isEmpty())
        assertEquals(3, pastEnd.totalCount)
        assertFalse(pastEnd.hasNextPage)
        assertTrue(api.detailCalls.isEmpty())
        assertEquals(2, api.pageCalls.size)
    }

    @Test
    fun refreshReloadsIndexAndDetails() = runTest {
        val api = FilteringApi()
        val repository = PokemonRepositoryImpl(api, StandardTestDispatcher(testScheduler))
        val filters = CatalogFilters(type = "electric")
        repository.getPokemonPage(1, 0, filters = filters)
        repository.getPokemonPage(1, 0, refresh = true, filters = filters)

        assertEquals(listOf("electric", "electric"), api.typeCalls)
        assertEquals(listOf(25, 25), api.detailCalls)
    }

    @Test
    fun failedIndexIsNotCachedAndCanBeRetried() = runTest {
        val api = FilteringApi()
        val repository = PokemonRepositoryImpl(api, StandardTestDispatcher(testScheduler))
        api.offline = true
        try {
            repository.getPokemonPage(20, 0, filters = CatalogFilters(type = "electric"))
            fail("Expected network error")
        } catch (error: CatalogException) {
            assertEquals(CatalogError.NETWORK, error.error)
        }
        api.offline = false

        assertEquals(3, repository.getPokemonPage(20, 0, filters = CatalogFilters(type = "electric")).totalCount)
        assertEquals(2, api.typeCalls.size)
    }

    private class FilteringApi : PokemonApi {
        val pageCalls = mutableListOf<Pair<Int, Int>>()
        val typeCalls = mutableListOf<String>()
        val detailCalls = mutableListOf<Int>()
        var offline = false

        override suspend fun getPokemonPage(limit: Int, offset: Int): PokemonPageDto {
            pageCalls += limit to offset
            return if (offset == 0) {
                PokemonPageDto(4, "next", null, listOf(resource(1, "bulbasaur"), resource(25, "pikachu")))
            } else {
                PokemonPageDto(4, null, "previous", listOf(resource(26, "pikachu-rock-star"), resource(27, "pikachu-pop-star")))
            }
        }

        override suspend fun getPokemonType(type: String): PokemonTypeResponseDto {
            typeCalls += type
            if (offline) throw IOException("Offline")
            return PokemonTypeResponseDto(listOf(
                resource(25, "pikachu"), resource(26, "pikachu-rock-star"), resource(81, "magnemite"),
            ).map { PokemonTypeMemberDto(it) })
        }

        override suspend fun getPokemonDetails(id: Int) = pokemonDto(id).also { detailCalls += id }

        private fun resource(id: Int, name: String) =
            NamedResourceDto(name, "https://pokeapi.co/api/v2/pokemon/$id/")
    }
}
