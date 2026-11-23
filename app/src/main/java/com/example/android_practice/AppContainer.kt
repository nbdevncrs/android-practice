package com.example.android_practice

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.android_practice.catalog.data.remote.PokemonApiFactory
import com.example.android_practice.catalog.data.repository.DataStoreCatalogFiltersRepository
import com.example.android_practice.catalog.data.repository.PokemonRepositoryImpl
import com.example.android_practice.catalog.domain.repository.CatalogFiltersRepository
import com.example.android_practice.catalog.domain.usecase.GetPokemonDetails
import com.example.android_practice.catalog.domain.usecase.GetPokemonPage
import com.example.android_practice.catalog.presentation.filters.FilterBadgeCache
import com.example.android_practice.core.database.PracticeDatabase
import com.example.android_practice.core.diagnostics.withNetworkDiagnostics
import com.example.android_practice.core.storage.applicationPreferences
import com.example.android_practice.favorites.data.local.FileFavoriteArtworkStore
import com.example.android_practice.favorites.data.repository.RoomFavoritesRepository
import com.example.android_practice.favorites.domain.repository.FavoritesRepository
import com.example.android_practice.profile.data.ProfileData
import com.example.android_practice.profile.domain.usecase.DiscardUnusedAvatar
import com.example.android_practice.profile.domain.usecase.SaveProfileAndScheduleReminder
import java.io.File
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppContainer(context: Context) {
    private val applicationContext = context.applicationContext
    internal val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val preferences by lazy { applicationPreferences(applicationContext) }

    val catalogFiltersRepository: CatalogFiltersRepository by lazy {
        DataStoreCatalogFiltersRepository(preferences)
    }
    val filterBadgeCache by lazy { FilterBadgeCache(catalogFiltersRepository, applicationScope) }

    val profileData by lazy {
        ProfileData(applicationContext, preferences, client) {
            val intent = Intent(applicationContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            PendingIntent.getActivity(
                applicationContext,
                OPEN_APP_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }
    }
    val discardUnusedAvatar by lazy { DiscardUnusedAvatar(profileData.profiles, profileData.avatars) }
    val saveProfileAndScheduleReminder by lazy {
        SaveProfileAndScheduleReminder(profileData.profiles, profileData.reminderScheduler, profileData.clock)
    }

    private val database by lazy { PracticeDatabase.create(applicationContext) }
    val favoritesRepository: FavoritesRepository by lazy {
        RoomFavoritesRepository(
            dao = database.favoritePokemonDao(),
            artworkStore = FileFavoriteArtworkStore(
                File(applicationContext.filesDir, "favorite_artwork"),
                client.newBuilder().callTimeout(15, TimeUnit.SECONDS).build(),
            ),
        )
    }

    private val client = PokemonApiFactory.clientBuilder()
        .withNetworkDiagnostics(applicationContext)
        .build()
    private val repository = PokemonRepositoryImpl(PokemonApiFactory.create(client))

    val getPokemonPage = GetPokemonPage(repository)
    val getPokemonDetails = GetPokemonDetails(repository)

    private companion object {
        const val OPEN_APP_REQUEST_CODE = 701
    }
}
