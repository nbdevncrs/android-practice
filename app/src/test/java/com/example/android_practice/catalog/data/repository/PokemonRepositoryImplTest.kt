package com.example.android_practice.catalog.data.repository

import com.example.android_practice.catalog.data.pokemonDto
import com.example.android_practice.catalog.data.remote.PokemonApi
import com.example.android_practice.catalog.data.remote.dto.NamedResourceDto
import com.example.android_practice.catalog.data.remote.dto.PokemonDetailsDto
import com.example.android_practice.catalog.data.remote.dto.PokemonPageDto
import com.example.android_practice.catalog.domain.model.CatalogError
import com.example.android_practice.catalog.domain.model.CatalogException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class PokemonRepositoryImplTest {
    @Test
    fun pageLimitsConcurrentRequestsAndPreservesServerOrder() = runTest {
        val api = FakePokemonApi(listOf(3, 1, 2, 5, 4, 7, 6))
        var active = 0
        var peak = 0
        api.detailsHandler = { id ->
            active++
            peak = maxOf(peak, active)
            try {
                delay(100)
                pokemonDto(id)
            } finally {
                active--
            }
        }
        val repository = PokemonRepositoryImpl(api, StandardTestDispatcher(testScheduler), maxConcurrentRequests = 2)

        val page = repository.getPokemonPage(7, 0)

        assertEquals(listOf(3, 1, 2, 5, 4, 7, 6), page.items.map { it.id })
        assertEquals(2, peak)
        assertEquals(7, page.totalCount)
        assertFalse(page.hasNextPage)
        assertEquals(7 to 0, api.pageArguments)
    }

    @Test
    fun detailsReusePageCacheAndRefreshFetchesNewData() = runTest {
        val api = FakePokemonApi(listOf(1))
        val repository = PokemonRepositoryImpl(api, StandardTestDispatcher(testScheduler))
        repository.getPokemonPage(20, 0)
        api.detailsHandler = { id -> pokemonDto(id).copy(name = "updated") }

        assertEquals("pokemon-1", repository.getPokemonDetails(1).name)
        assertEquals(listOf(1), api.detailsCalls)
        assertEquals("updated", repository.getPokemonDetails(1, refresh = true).name)
        assertEquals("updated", repository.getPokemonDetails(1).name)
        assertEquals(listOf(1, 1), api.detailsCalls)
    }

    @Test
    fun pageRefreshBypassesCachedDetails() = runTest {
        val api = FakePokemonApi(listOf(1))
        val repository = PokemonRepositoryImpl(api, StandardTestDispatcher(testScheduler))
        repository.getPokemonPage(20, 0)
        api.detailsHandler = { id -> pokemonDto(id).copy(name = "updated") }

        assertEquals("updated", repository.getPokemonPage(20, 0, refresh = true).items.single().name)
        assertEquals(listOf(1, 1), api.detailsCalls)
    }

    @Test
    fun boundedCacheEvictsLeastRecentlyUsedEntry() = runTest {
        val api = FakePokemonApi()
        val repository = PokemonRepositoryImpl(api, StandardTestDispatcher(testScheduler), cacheCapacity = 2)
        repository.getPokemonDetails(1)
        repository.getPokemonDetails(2)
        repository.getPokemonDetails(1)
        repository.getPokemonDetails(3)
        repository.getPokemonDetails(2)

        assertEquals(listOf(1, 2, 3, 2), api.detailsCalls)
    }

    @Test
    fun networkFailureIsTranslatedAndFailedResponseIsNotCached() = runTest {
        val api = FakePokemonApi()
        val repository = PokemonRepositoryImpl(api, StandardTestDispatcher(testScheduler))
        api.detailsHandler = { throw IOException("Offline") }

        expectError(CatalogError.NETWORK) { repository.getPokemonDetails(1) }
        api.detailsHandler = { pokemonDto(it) }
        assertEquals(1, repository.getPokemonDetails(1).id)
        assertEquals(listOf(1, 1), api.detailsCalls)
    }

    @Test
    fun httpErrorsAreTranslated() = runTest {
        val api = FakePokemonApi()
        val repository = PokemonRepositoryImpl(api, StandardTestDispatcher(testScheduler))
        api.detailsHandler = { throw HttpException(Response.error<PokemonDetailsDto>(404, "Missing".toResponseBody())) }
        expectError(CatalogError.NOT_FOUND) { repository.getPokemonDetails(1) }
        api.detailsHandler = { throw HttpException(Response.error<PokemonDetailsDto>(503, "Unavailable".toResponseBody())) }
        expectError(CatalogError.REMOTE) { repository.getPokemonDetails(1) }
    }

    @Test
    fun incompleteOrWrongDetailsAreRejected() = runTest {
        val api = FakePokemonApi()
        val repository = PokemonRepositoryImpl(api, StandardTestDispatcher(testScheduler))
        api.detailsHandler = { pokemonDto(it).copy(stats = emptyList()) }
        expectError(CatalogError.INVALID_DATA) { repository.getPokemonDetails(1) }
        api.detailsHandler = { pokemonDto(2) }
        expectError(CatalogError.INVALID_DATA) { repository.getPokemonDetails(1) }
    }

    @Test
    fun failedItemFailsThePageInsteadOfDroppingIt() = runTest {
        val api = FakePokemonApi(listOf(1, 2))
        val repository = PokemonRepositoryImpl(api, StandardTestDispatcher(testScheduler))
        api.detailsHandler = { id -> if (id == 2) throw IOException("Offline") else pokemonDto(id) }

        expectError(CatalogError.NETWORK) { repository.getPokemonPage(20, 0) }
    }

    @Test
    fun cancellationIsPropagatedWithoutWrappingAsNetworkError() = runTest {
        val api = FakePokemonApi()
        val repository = PokemonRepositoryImpl(api, StandardTestDispatcher(testScheduler))
        api.detailsHandler = { throw CancellationException("Cancelled") }

        try {
            repository.getPokemonDetails(1)
            fail("Expected cancellation")
        } catch (error: CancellationException) {
            assertEquals("Cancelled", error.message)
        }
        api.detailsHandler = { pokemonDto(it) }
        assertEquals(1, repository.getPokemonDetails(1).id)
    }

    private suspend fun expectError(expected: CatalogError, block: suspend () -> Any) {
        try {
            block()
            fail("Expected $expected")
        } catch (error: CatalogException) {
            assertEquals(expected, error.error)
            assertTrue(error.cause != null)
        }
    }

    private class FakePokemonApi(private val ids: List<Int> = emptyList()) : PokemonApi {
        var detailsHandler: suspend (Int) -> PokemonDetailsDto = { pokemonDto(it) }
        val detailsCalls = mutableListOf<Int>()
        var pageArguments: Pair<Int, Int>? = null

        override suspend fun getPokemonPage(limit: Int, offset: Int): PokemonPageDto {
            pageArguments = limit to offset
            return PokemonPageDto(ids.size, null, null, ids.map {
                NamedResourceDto("pokemon-$it", "https://pokeapi.co/api/v2/pokemon/$it/")
            })
        }

        override suspend fun getPokemonDetails(id: Int): PokemonDetailsDto {
            detailsCalls += id
            return detailsHandler(id)
        }
    }
}
