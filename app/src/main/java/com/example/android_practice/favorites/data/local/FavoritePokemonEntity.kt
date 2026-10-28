package com.example.android_practice.favorites.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_pokemon")
data class FavoritePokemonEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val typesJson: String,
    val imageUrl: String?,
    val heightDecimeters: Int,
    val weightHectograms: Int,
    val abilitiesJson: String,
    val hp: Int,
    val attack: Int,
    val defense: Int,
    val specialAttack: Int,
    val specialDefense: Int,
    val speed: Int,
)
