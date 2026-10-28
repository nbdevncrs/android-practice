package com.example.android_practice.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.android_practice.favorites.data.local.FavoritePokemonDao
import com.example.android_practice.favorites.data.local.FavoritePokemonEntity

@Database(entities = [FavoritePokemonEntity::class], version = 1, exportSchema = true)
abstract class PracticeDatabase : RoomDatabase() {
    abstract fun favoritePokemonDao(): FavoritePokemonDao

    companion object {
        fun create(context: Context): PracticeDatabase = Room.databaseBuilder(
            context.applicationContext,
            PracticeDatabase::class.java,
            "practice.db",
        ).build()
    }
}
