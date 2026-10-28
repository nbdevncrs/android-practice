package com.example.android_practice.catalog.data.remote

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import java.util.concurrent.TimeUnit

class PokemonApiTest {
    private val server = MockWebServer()
    private lateinit var api: PokemonApi

    @Before
    fun setUp() {
        server.start()
        api = PokemonApiFactory.create(baseUrl = server.url("/api/v2/").toString())
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun pageRequestSendsPaginationAndParsesNullableLinks() = runBlocking {
        server.enqueue(MockResponse().setBody("""
            {
              "count": 1, "next": null, "previous": null,
              "results": [{"name": "pikachu", "url": "https://pokeapi.co/api/v2/pokemon/25/"}]
            }
        """.trimIndent()))

        val page = api.getPokemonPage(limit = 20, offset = 40)

        assertEquals("/api/v2/pokemon/?limit=20&offset=40", server.takeRequest(5, TimeUnit.SECONDS)?.path)
        assertEquals("pikachu", page.results.single().name)
        assertNull(page.next)
        assertNull(page.previous)
    }

    @Test
    fun detailsRequestParsesNestedFieldsAndIgnoresUnusedData() = runBlocking {
        server.enqueue(MockResponse().setBody("""
            {
              "id": 25, "name": "pikachu", "height": 4, "weight": 60,
              "types": [{"slot": 1, "type": {"name": "electric", "url": "https://pokeapi.co/api/v2/type/13/"}}],
              "abilities": [{"is_hidden": false, "slot": 1, "ability": {"name": "static", "url": "https://pokeapi.co/api/v2/ability/9/"}}],
              "stats": [{"base_stat": 35, "effort": 0, "stat": {"name": "hp", "url": "https://pokeapi.co/api/v2/stat/1/"}}],
              "sprites": {"front_default": null, "other": {"official-artwork": {"front_default": "https://example.com/pikachu.png"}}},
              "moves": [], "base_experience": 112
            }
        """.trimIndent()))

        val pokemon = api.getPokemonDetails(25)

        assertEquals("/api/v2/pokemon/25/", server.takeRequest(5, TimeUnit.SECONDS)?.path)
        assertEquals("electric", pokemon.types.single().type.name)
        assertEquals(35, pokemon.stats.single().baseStat)
        assertEquals(false, pokemon.abilities.single().isHidden)
        assertNull(pokemon.sprites.frontDefault)
        assertEquals("https://example.com/pikachu.png", pokemon.sprites.other?.officialArtwork?.frontDefault)
    }

    @Test(expected = HttpException::class)
    fun unsuccessfulResponseIsReportedToCaller(): Unit = runBlocking {
        server.enqueue(MockResponse().setResponseCode(404))
        api.getPokemonDetails(0)
    }
}
