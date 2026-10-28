package com.example.android_practice.catalog.domain.model

import java.util.Locale

enum class PokemonType {
    NORMAL, FIRE, WATER, ELECTRIC, GRASS, ICE, FIGHTING, POISON, GROUND,
    FLYING, PSYCHIC, BUG, ROCK, GHOST, DRAGON, DARK, STEEL, FAIRY, STELLAR;

    val apiName: String get() = name.lowercase(Locale.ROOT)
}
