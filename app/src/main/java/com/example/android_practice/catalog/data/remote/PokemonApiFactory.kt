package com.example.android_practice.catalog.data.remote

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

object PokemonApiFactory {
    private val json = Json { ignoreUnknownKeys = true }

    fun create(
        client: OkHttpClient = clientBuilder().build(),
        baseUrl: String = "https://pokeapi.co/api/v2/",
    ): PokemonApi = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(
            json.asConverterFactory("application/json".toMediaType()),
        )
        .build()
        .create(PokemonApi::class.java)

    fun clientBuilder(): OkHttpClient.Builder = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .callTimeout(45, TimeUnit.SECONDS)
}
