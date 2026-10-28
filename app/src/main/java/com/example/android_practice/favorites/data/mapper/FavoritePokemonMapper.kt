package com.example.android_practice.favorites.data.mapper

import com.example.android_practice.catalog.domain.model.PokemonAbility
import com.example.android_practice.catalog.domain.model.PokemonDetails
import com.example.android_practice.catalog.domain.model.PokemonStats
import com.example.android_practice.catalog.domain.model.PokemonSummary
import com.example.android_practice.favorites.data.local.FavoritePokemonEntity
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val storageJson = Json { ignoreUnknownKeys = true }

@Serializable
private data class SavedAbility(val name: String, val isHidden: Boolean)

internal fun PokemonDetails.toFavoriteEntity() = FavoritePokemonEntity(
    id = id,
    name = name,
    typesJson = storageJson.encodeToString(types),
    imageUrl = imageUrl,
    heightDecimeters = heightDecimeters,
    weightHectograms = weightHectograms,
    abilitiesJson = storageJson.encodeToString(abilities.map { SavedAbility(it.name, it.isHidden) }),
    hp = stats.hp,
    attack = stats.attack,
    defense = stats.defense,
    specialAttack = stats.specialAttack,
    specialDefense = stats.specialDefense,
    speed = stats.speed,
)

internal fun FavoritePokemonEntity.toDetails() = PokemonDetails(
    id = id,
    name = name,
    types = storageJson.decodeFromString<List<String>>(typesJson),
    imageUrl = imageUrl,
    heightDecimeters = heightDecimeters,
    weightHectograms = weightHectograms,
    abilities = storageJson.decodeFromString<List<SavedAbility>>(abilitiesJson).map { PokemonAbility(it.name, it.isHidden) },
    stats = PokemonStats(hp, attack, defense, specialAttack, specialDefense, speed),
)

internal fun FavoritePokemonEntity.toSummary() = PokemonSummary(
    id = id,
    name = name,
    types = storageJson.decodeFromString<List<String>>(typesJson),
    imageUrl = imageUrl,
)
