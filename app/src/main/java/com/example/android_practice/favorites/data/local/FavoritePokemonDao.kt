package com.example.android_practice.favorites.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritePokemonDao {
    @Query("SELECT * FROM favorite_pokemon ORDER BY id ASC")
    fun observeAll(): Flow<List<FavoritePokemonEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_pokemon WHERE id = :id)")
    fun observeExists(id: Int): Flow<Boolean>

    @Query("SELECT * FROM favorite_pokemon WHERE id = :id")
    suspend fun findById(id: Int): FavoritePokemonEntity?

    @Upsert
    suspend fun upsert(pokemon: FavoritePokemonEntity)

    @Query("DELETE FROM favorite_pokemon WHERE id = :id")
    suspend fun deleteById(id: Int)
}
