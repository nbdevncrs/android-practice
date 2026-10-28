package com.example.android_practice.favorites.data.repository

import android.database.SQLException
import com.example.android_practice.catalog.domain.model.PokemonDetails
import com.example.android_practice.favorites.data.local.FavoriteArtworkStore
import com.example.android_practice.favorites.data.local.FavoritePokemonDao
import com.example.android_practice.favorites.data.mapper.toDetails
import com.example.android_practice.favorites.data.mapper.toFavoriteEntity
import com.example.android_practice.favorites.data.mapper.toSummary
import com.example.android_practice.favorites.domain.model.FavoritesStorageException
import com.example.android_practice.favorites.domain.repository.FavoritesRepository
import java.io.IOException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException

class RoomFavoritesRepository(
    private val dao: FavoritePokemonDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val artworkStore: FavoriteArtworkStore? = null,
) : FavoritesRepository {
    private val writeMutex = Mutex()

    override fun observeFavorites() = dao.observeAll()
        .map { entities -> entities.map { it.toSummary().copy(localImagePath = artworkStore?.find(it.id)) } }
        .distinctUntilChanged()
        .catch { throw it.asStorageFailure() }
        .flowOn(ioDispatcher)

    override fun observeIsFavorite(id: Int) = dao.observeExists(id).distinctUntilChanged()
        .catch { throw it.asStorageFailure() }

    override suspend fun getPokemonDetails(id: Int) = storageOperation {
        dao.findById(id)?.toDetails()?.copy(localImagePath = artworkStore?.find(id))
    }

    override suspend fun save(pokemon: PokemonDetails) = storageOperation {
        require(pokemon.id > 0)
        writeMutex.withLock {
            try {
                artworkStore?.save(pokemon.id, pokemon.imageUrl)
            } catch (_: IOException) {
            }
            dao.upsert(pokemon.toFavoriteEntity())
        }
    }

    override suspend fun remove(id: Int) = storageOperation {
        writeMutex.withLock {
            dao.deleteById(id)
            try {
                artworkStore?.remove(id)
            } catch (_: IOException) {
            }
            Unit
        }
    }

    private suspend fun <T> storageOperation(block: suspend () -> T): T = withContext(ioDispatcher) {
        try {
            block()
        } catch (error: Exception) {
            throw error.asStorageFailure()
        }
    }

    private fun Throwable.asStorageFailure(): Throwable = when (this) {
        is IOException, is SQLException, is SerializationException -> FavoritesStorageException(this)
        else -> this
    }
}
