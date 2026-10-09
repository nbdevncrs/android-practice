package com.example.android_practice.catalog.data.cache

import com.example.android_practice.catalog.domain.model.PokemonDetails
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class PokemonMemoryCache(private val capacity: Int) {
    init {
        require(capacity > 0)
    }

    private val mutex = Mutex()
    private val entries = linkedMapOf<Int, PokemonDetails>()

    suspend fun get(id: Int): PokemonDetails? = mutex.withLock {
        entries.remove(id)?.also { entries[id] = it }
    }

    suspend fun put(pokemon: PokemonDetails) = mutex.withLock {
        entries.remove(pokemon.id)
        entries[pokemon.id] = pokemon
        if (entries.size > capacity) entries.remove(entries.keys.first())
        Unit
    }
}
