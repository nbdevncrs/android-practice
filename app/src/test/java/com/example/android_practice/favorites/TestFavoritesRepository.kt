package com.example.android_practice.favorites

import com.example.android_practice.catalog.domain.model.PokemonDetails
import com.example.android_practice.catalog.domain.model.PokemonSummary
import com.example.android_practice.favorites.domain.repository.FavoritesRepository
import com.example.android_practice.favorites.domain.model.FavoritesStorageException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.io.IOException

internal class TestFavoritesRepository : FavoritesRepository {
    val saved = MutableStateFlow<Map<Int, PokemonDetails>>(emptyMap())
    var writeError: IOException? = null
    override fun observeFavorites() = saved.map { values ->
        values.values.sortedBy { it.id }.map { PokemonSummary(it.id, it.name, it.types, it.imageUrl, it.localImagePath) }
    }
    override fun observeIsFavorite(id: Int) = saved.map { id in it }
    override suspend fun getPokemonDetails(id: Int) = saved.value[id]
    override suspend fun save(pokemon: PokemonDetails) {
        writeError?.let { throw FavoritesStorageException(it) }
        saved.value += pokemon.id to pokemon
    }
    override suspend fun remove(id: Int) {
        writeError?.let { throw FavoritesStorageException(it) }
        saved.value -= id
    }
}
