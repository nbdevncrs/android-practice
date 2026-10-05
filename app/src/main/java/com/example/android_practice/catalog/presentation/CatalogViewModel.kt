package com.example.android_practice.catalog.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.android_practice.catalog.domain.PokemonRepository
import com.example.android_practice.catalog.domain.PokemonSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CatalogViewModel(private val repository: PokemonRepository) : ViewModel() {
    private val _pokemon = MutableStateFlow<List<PokemonSummary>>(emptyList())
    val pokemon = _pokemon.asStateFlow()

    init {
        viewModelScope.launch { _pokemon.value = repository.getPokemon() }
    }

    companion object {
        fun factory(repository: PokemonRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    CatalogViewModel(repository) as T
            }
    }
}
