package com.example.android_practice.favorites.data.local

interface FavoriteArtworkStore {
    suspend fun save(id: Int, imageUrl: String?)
    fun find(id: Int): String?
    fun remove(id: Int)
}
