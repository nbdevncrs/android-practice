package com.example.android_practice.catalog.data.remote

import com.example.android_practice.catalog.data.remote.dto.PokemonDetailsDto
import com.example.android_practice.catalog.data.remote.dto.PokemonPageDto
import com.example.android_practice.catalog.data.remote.dto.PokemonTypeResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PokemonApi {
    @GET("pokemon/")
    suspend fun getPokemonPage(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
    ): PokemonPageDto

    @GET("pokemon/{id}/")
    suspend fun getPokemonDetails(@Path("id") id: Int): PokemonDetailsDto

    @GET("type/{type}/")
    suspend fun getPokemonType(@Path("type") type: String): PokemonTypeResponseDto
}
