package com.example.android_practice.catalog.data.repository

import com.example.android_practice.catalog.data.cache.PokemonMemoryCache
import com.example.android_practice.catalog.data.mapper.InvalidPokemonDataException
import com.example.android_practice.catalog.data.mapper.pokemonId
import com.example.android_practice.catalog.data.mapper.toDomain
import com.example.android_practice.catalog.data.mapper.toSummary
import com.example.android_practice.catalog.data.remote.PokemonApi
import com.example.android_practice.catalog.domain.model.CatalogError
import com.example.android_practice.catalog.domain.model.CatalogException
import com.example.android_practice.catalog.domain.model.PokemonDetails
import com.example.android_practice.catalog.domain.model.PokemonPage
import com.example.android_practice.catalog.domain.repository.PokemonRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

class PokemonRepositoryImpl(
    private val api: PokemonApi,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    maxConcurrentRequests: Int = 4,
    cacheCapacity: Int = 100,
) : PokemonRepository {
    init {
        require(maxConcurrentRequests > 0)
    }

    private val requests = Semaphore(maxConcurrentRequests)
    private val cache = PokemonMemoryCache(cacheCapacity)

    override suspend fun getPokemonPage(limit: Int, offset: Int, refresh: Boolean): PokemonPage {
        require(limit > 0)
        require(offset >= 0)
        return withContext(ioDispatcher) {
            translateErrors {
                val page = requests.withPermit { api.getPokemonPage(limit, offset) }
                val items = coroutineScope {
                    page.results.map { resource ->
                        async { getPokemonDetails(resource.pokemonId(), refresh).toSummary() }
                    }.awaitAll()
                }
                PokemonPage(items, page.count, hasNextPage = page.next != null)
            }
        }
    }

    override suspend fun getPokemonDetails(id: Int, refresh: Boolean): PokemonDetails {
        require(id > 0)
        return withContext(ioDispatcher) {
            translateErrors {
                if (!refresh) cache.get(id)?.let { return@translateErrors it }
                requests.withPermit {
                    if (!refresh) cache.get(id)?.let { return@withPermit it }
                    val pokemon = api.getPokemonDetails(id).toDomain()
                    if (pokemon.id != id) throw InvalidPokemonDataException("Unexpected Pokémon ID")
                    cache.put(pokemon)
                    pokemon
                }
            }
        }
    }

    private suspend fun <T> translateErrors(block: suspend () -> T): T = try {
        block()
    } catch (error: HttpException) {
        throw CatalogException(if (error.code() == 404) CatalogError.NOT_FOUND else CatalogError.REMOTE, error)
    } catch (error: IOException) {
        throw CatalogException(CatalogError.NETWORK, error)
    } catch (error: SerializationException) {
        throw CatalogException(CatalogError.INVALID_DATA, error)
    } catch (error: InvalidPokemonDataException) {
        throw CatalogException(CatalogError.INVALID_DATA, error)
    }
}
