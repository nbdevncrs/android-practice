package com.example.android_practice.favorites.data.mapper

import com.example.android_practice.catalog.domain.model.PokemonAbility
import com.example.android_practice.catalog.presentation.details
import org.junit.Assert.assertEquals
import org.junit.Test

class FavoritePokemonMapperTest {
    @Test
    fun snapshotPreservesAllDetailsAndSpecialCharacters() {
        val pokemon = details().copy(
            name = "mr-mime",
            types = listOf("psychic", "fairy"),
            imageUrl = "https://example.com/122.png",
            abilities = listOf(PokemonAbility("ability\"name", false), PokemonAbility("hidden-ability", true)),
        )

        assertEquals(pokemon, pokemon.toFavoriteEntity().toDetails())
        assertEquals(pokemon.types, pokemon.toFavoriteEntity().toSummary().types)
    }

    @Test
    fun missingImageAndEmptyCollectionsArePreserved() {
        val pokemon = details().copy(types = emptyList(), abilities = emptyList(), imageUrl = null)

        assertEquals(pokemon, pokemon.toFavoriteEntity().toDetails())
    }
}
