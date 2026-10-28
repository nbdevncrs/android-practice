package com.example.android_practice.favorites.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.android_practice.favorites.domain.repository.FavoritesRepository
import com.example.android_practice.favorites.domain.model.FavoritesStorageException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavoritesViewModel(private val repository: FavoritesRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<FavoritesUiState>(FavoritesUiState.Loading)
    val uiState = _uiState.asStateFlow()
    private var observeJob: Job? = null

    init {
        retry()
    }

    fun retry() {
        observeJob?.cancel()
        _uiState.value = FavoritesUiState.Loading
        observeJob = viewModelScope.launch {
            try {
                repository.observeFavorites().collect { _uiState.value = FavoritesUiState.Content(it) }
            } catch (error: CancellationException) {
                throw error
            } catch (_: FavoritesStorageException) {
                _uiState.value = FavoritesUiState.Error
            }
        }
    }

    companion object {
        fun factory(repository: FavoritesRepository) = viewModelFactory {
            initializer { FavoritesViewModel(repository) }
        }
    }
}
