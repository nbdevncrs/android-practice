package com.example.android_practice.catalog.data.mapper

import com.example.android_practice.catalog.data.pokemonDto
import com.example.android_practice.catalog.data.remote.dto.NamedResourceDto
import com.example.android_practice.catalog.data.remote.dto.PokemonSpritesDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PokemonMapperTest {
    @Test
    fun mapsStatsByNameAndTypesAndAbilitiesBySlot() {
        val pokemon = pokemonDto().toDomain()

        assertEquals(listOf("grass", "poison"), pokemon.types)
        assertEquals(listOf("overgrow", "chlorophyll"), pokemon.abilities.map { it.name })
        assertEquals(listOf(false, true), pokemon.abilities.map { it.isHidden })
        assertEquals(45, pokemon.stats.hp)
        assertEquals(49, pokemon.stats.attack)
        assertEquals(49, pokemon.stats.defense)
        assertEquals(65, pokemon.stats.specialAttack)
        assertEquals(65, pokemon.stats.specialDefense)
        assertEquals(45, pokemon.stats.speed)
        assertEquals(7, pokemon.heightDecimeters)
        assertEquals(69, pokemon.weightHectograms)
        assertEquals("https://example.com/artwork.png", pokemon.imageUrl)
    }

    @Test
    fun fallsBackToSpriteAndAllowsMissingImages() {
        val spriteOnly = pokemonDto().copy(sprites = PokemonSpritesDto("https://example.com/sprite.png"))

        assertEquals("https://example.com/sprite.png", spriteOnly.toDomain().imageUrl)
        assertNull(pokemonDto().copy(sprites = PokemonSpritesDto()).toDomain().imageUrl)
    }

    @Test(expected = InvalidPokemonDataException::class)
    fun missingStatIsRejectedInsteadOfSilentlyDisplayingZero() {
        pokemonDto().copy(stats = emptyList()).toDomain()
    }

    @Test
    fun extractsIdWithOrWithoutTrailingSlash() {
        assertEquals(25, NamedResourceDto("pikachu", "https://pokeapi.co/api/v2/pokemon/25/").pokemonId())
        assertEquals(25, NamedResourceDto("pikachu", "https://pokeapi.co/api/v2/pokemon/25").pokemonId())
    }

    @Test(expected = InvalidPokemonDataException::class)
    fun invalidResourceIdIsRejected() {
        NamedResourceDto("pikachu", "https://pokeapi.co/api/v2/pokemon/not-an-id/").pokemonId()
    }
}
