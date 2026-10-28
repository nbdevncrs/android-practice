package com.example.android_practice.catalog.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.android_practice.catalog.domain.model.CatalogException
import com.example.android_practice.catalog.domain.model.CatalogError
import com.example.android_practice.catalog.domain.model.CatalogFilters
import com.example.android_practice.catalog.domain.repository.CatalogFiltersRepository
import com.example.android_practice.catalog.domain.usecase.GetPokemonPage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.IOException

class CatalogViewModel(
    private val getPokemonPage: GetPokemonPage,
    private val filtersRepository: CatalogFiltersRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<CatalogUiState>(CatalogUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var lastRequest: PageRequest? = null

    init {
        reloadFilters()
    }

    fun reloadFilters() = startLoad {
        PageRequest(filters = filtersRepository.filters.first().normalized())
    }

    fun loadPage(
        limit: Int = 20,
        offset: Int = 0,
        refresh: Boolean = false,
        filters: CatalogFilters = CatalogFilters(),
    ) {
        require(limit > 0)
        require(offset >= 0)
        startLoad { PageRequest(limit, offset, refresh, filters.normalized()) }
    }

    private fun startLoad(requestProvider: suspend () -> PageRequest) {
        loadJob?.cancel()
        _uiState.value = CatalogUiState.Loading
        loadJob = viewModelScope.launch {
            try {
                val request = requestProvider()
                currentCoroutineContext().ensureActive()
                lastRequest = request
                val page = getPokemonPage(request.limit, request.offset, request.refresh, request.filters)
                currentCoroutineContext().ensureActive()
                _uiState.value = CatalogUiState.Content(page)
            } catch (error: CancellationException) {
                throw error
            } catch (error: CatalogException) {
                currentCoroutineContext().ensureActive()
                _uiState.value = CatalogUiState.Error(error.error)
            } catch (_: IOException) {
                currentCoroutineContext().ensureActive()
                _uiState.value = CatalogUiState.Error(CatalogError.STORAGE)
            }
        }
    }

    fun retry() {
        val request = lastRequest
        if (request == null || (_uiState.value as? CatalogUiState.Error)?.error == CatalogError.STORAGE) reloadFilters()
        else startLoad { request }
    }

    fun refresh() {
        val request = lastRequest
        if (request == null) reloadFilters() else startLoad { request.copy(refresh = true) }
    }

    private data class PageRequest(
        val limit: Int = 20,
        val offset: Int = 0,
        val refresh: Boolean = false,
        val filters: CatalogFilters = CatalogFilters(),
    )

    companion object {
        fun factory(getPokemonPage: GetPokemonPage, filtersRepository: CatalogFiltersRepository) = viewModelFactory {
            initializer { CatalogViewModel(getPokemonPage, filtersRepository) }
        }
    }
}
