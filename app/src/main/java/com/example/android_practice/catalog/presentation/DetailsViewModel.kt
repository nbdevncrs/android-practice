package com.example.android_practice.catalog.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.android_practice.catalog.domain.PokemonDetails
import com.example.android_practice.catalog.domain.PokemonRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetailsViewModel(repository: PokemonRepository, pokemonId: Int) : ViewModel() {
    private val _pokemon = MutableStateFlow<PokemonDetails?>(null)
    val pokemon = _pokemon.asStateFlow()

    init {
        viewModelScope.launch { _pokemon.value = repository.getPokemonDetails(pokemonId) }
    }

    companion object {
        fun factory(repository: PokemonRepository, pokemonId: Int): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    DetailsViewModel(repository, pokemonId) as T
            }
    }
}
