package com.example.android_practice.favorites.data

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.android_practice.catalog.domain.model.PokemonAbility
import com.example.android_practice.catalog.domain.model.PokemonDetails
import com.example.android_practice.catalog.domain.model.PokemonStats
import com.example.android_practice.core.database.PracticeDatabase
import com.example.android_practice.favorites.data.repository.RoomFavoritesRepository
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class RoomFavoritesRepositoryTest {
    private lateinit var context: Context
    private lateinit var database: PracticeDatabase
    private lateinit var repository: RoomFavoritesRepository
    private val databaseName = "favorites-test-${UUID.randomUUID()}.db"

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        openDatabase()
    }

    @After
    fun tearDown() {
        database.close()
        context.deleteDatabase(databaseName)
    }

    @Test
    fun allDetailsSurviveDatabaseReopening() = runBlocking {
        val pokemon = pokemon(25)
        repository.save(pokemon)
        database.close()
        openDatabase()

        assertEquals(pokemon, repository.getPokemonDetails(25))
        assertEquals(listOf(25), repository.observeFavorites().first().map { it.id })
        assertTrue(repository.observeIsFavorite(25).first())
    }

    @Test
    fun savingTwiceUpdatesExistingEntryAndKeepsOrder() = runBlocking {
        repository.save(pokemon(25))
        repository.save(pokemon(1))
        val updated = pokemon(25).copy(name = "updated")
        repository.save(updated)

        assertEquals(listOf(1, 25), repository.observeFavorites().first().map { it.id })
        assertEquals(updated, repository.getPokemonDetails(25))
    }

    @Test
    fun observersReceiveRemovalAndMissingDetailsReturnNull() = runBlocking {
        repository.save(pokemon(25))
        val removed = async(start = CoroutineStart.UNDISPATCHED) {
            withTimeout(5_000) { repository.observeIsFavorite(25).first { !it } }
        }
        repository.remove(25)

        assertFalse(removed.await())
        assertTrue(repository.observeFavorites().first().isEmpty())
        assertNull(repository.getPokemonDetails(25))
        repository.remove(25)
    }

    private fun openDatabase() {
        database = Room.databaseBuilder(context, PracticeDatabase::class.java, databaseName).build()
        repository = RoomFavoritesRepository(database.favoritePokemonDao())
    }

    private fun pokemon(id: Int) = PokemonDetails(
        id = id,
        name = "pokemon-$id",
        types = listOf("electric", "flying"),
        imageUrl = null,
        heightDecimeters = 4,
        weightHectograms = 60,
        abilities = listOf(PokemonAbility("static", false), PokemonAbility("lightning-rod", true)),
        stats = PokemonStats(35, 55, 40, 50, 50, 90),
    )
}
