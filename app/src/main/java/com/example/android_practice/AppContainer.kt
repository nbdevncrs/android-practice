package com.example.android_practice

import android.content.Context
import com.example.android_practice.core.diagnostics.withNetworkDiagnostics
import com.example.android_practice.catalog.data.remote.PokemonApiFactory
import com.example.android_practice.catalog.data.repository.PokemonRepositoryImpl
import com.example.android_practice.catalog.domain.usecase.GetPokemonDetails
import com.example.android_practice.catalog.domain.usecase.GetPokemonPage

class AppContainer(context: Context) {
    private val client = PokemonApiFactory.clientBuilder()
        .withNetworkDiagnostics(context.applicationContext)
        .build()
    private val repository = PokemonRepositoryImpl(PokemonApiFactory.create(client))

    val getPokemonPage = GetPokemonPage(repository)
    val getPokemonDetails = GetPokemonDetails(repository)
}
