package com.example.android_practice.catalog.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.android_practice.catalog.domain.model.CatalogException
import com.example.android_practice.catalog.domain.usecase.GetPokemonPage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CatalogViewModel(private val getPokemonPage: GetPokemonPage) : ViewModel() {
    private val _uiState = MutableStateFlow<CatalogUiState>(CatalogUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var lastRequest = PageRequest()

    init {
        loadPage()
    }

    fun loadPage(limit: Int = 20, offset: Int = 0, refresh: Boolean = false) {
        require(limit > 0)
        require(offset >= 0)
        lastRequest = PageRequest(limit, offset, refresh)
        loadJob?.cancel()
        _uiState.value = CatalogUiState.Loading
        loadJob = viewModelScope.launch {
            try {
                val page = getPokemonPage(limit, offset, refresh)
                currentCoroutineContext().ensureActive()
                _uiState.value = CatalogUiState.Content(page)
            } catch (error: CancellationException) {
                throw error
            } catch (error: CatalogException) {
                currentCoroutineContext().ensureActive()
                _uiState.value = CatalogUiState.Error(error.error)
            }
        }
    }

    fun retry() = loadPage(lastRequest.limit, lastRequest.offset, lastRequest.refresh)

    fun refresh() = loadPage(lastRequest.limit, lastRequest.offset, refresh = true)

    private data class PageRequest(val limit: Int = 20, val offset: Int = 0, val refresh: Boolean = false)

    companion object {
        fun factory(getPokemonPage: GetPokemonPage) = viewModelFactory {
            initializer { CatalogViewModel(getPokemonPage) }
        }
    }
}
