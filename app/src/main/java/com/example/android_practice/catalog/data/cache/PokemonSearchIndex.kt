package com.example.android_practice.catalog.data.cache

import com.example.android_practice.catalog.data.mapper.InvalidPokemonDataException
import com.example.android_practice.catalog.data.remote.PokemonApi
import com.example.android_practice.catalog.data.remote.dto.NamedResourceDto
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit

internal class PokemonSearchIndex(
    private val api: PokemonApi,
    private val requests: Semaphore,
) {
    private val mutex = Mutex()
    private val entries = linkedMapOf<String?, List<NamedResourceDto>>()

    suspend fun get(type: String?, refresh: Boolean): List<NamedResourceDto> = mutex.withLock {
        if (!refresh) entries.remove(type)?.let {
            entries[type] = it
            return@withLock it
        }

        val resources = if (type == null) {
            loadAllNames()
        } else {
            requests.withPermit { api.getPokemonType(type) }.pokemon.map { it.pokemon }
        }
        entries.remove(type)
        entries[type] = resources
        if (entries.size > MAX_ENTRIES) entries.remove(entries.keys.first())
        resources
    }

    private suspend fun loadAllNames(): List<NamedResourceDto> {
        val resources = mutableListOf<NamedResourceDto>()
        do {
            val page = requests.withPermit { api.getPokemonPage(INDEX_PAGE_SIZE, resources.size) }
            if (page.results.isEmpty() && page.next != null) {
                throw InvalidPokemonDataException("Empty page before the end of the Pokémon index")
            }
            resources.addAll(page.results)
        } while (page.next != null)
        return resources
    }

    private companion object {
        const val INDEX_PAGE_SIZE = 500
        const val MAX_ENTRIES = 32
    }
}
