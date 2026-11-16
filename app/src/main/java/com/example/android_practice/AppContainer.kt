package com.example.android_practice

import android.content.Context
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
import com.example.android_practice.profile.data.repository.DataStoreProfileRepository
import com.example.android_practice.profile.domain.repository.ProfileRepository
import com.example.android_practice.profile.domain.repository.AvatarRepository
import com.example.android_practice.profile.data.repository.FileAvatarRepository
import com.example.android_practice.profile.data.repository.HttpResumeRepository
import com.example.android_practice.profile.domain.repository.ResumeRepository
import com.example.android_practice.profile.domain.usecase.DiscardUnusedAvatar
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

    val profileRepository: ProfileRepository by lazy { DataStoreProfileRepository(preferences) }
    val avatarRepository: AvatarRepository by lazy { FileAvatarRepository(applicationContext) }
    val discardUnusedAvatar by lazy { DiscardUnusedAvatar(profileRepository, avatarRepository) }
    val resumeRepository: ResumeRepository by lazy {
        HttpResumeRepository(File(applicationContext.cacheDir, "profile/resumes"), client)
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
}
