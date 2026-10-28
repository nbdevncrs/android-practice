package com.example.android_practice.catalog.presentation.filters

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.android_practice.catalog.domain.model.CatalogFilters
import com.example.android_practice.catalog.domain.repository.CatalogFiltersRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

class FiltersViewModel(
    private val repository: CatalogFiltersRepository,
    private val savedStateHandle: SavedStateHandle,
    badgeCache: FilterBadgeCache,
) : ViewModel() {
    private val _uiState = MutableStateFlow(FiltersUiState())
    val uiState = _uiState.asStateFlow()
    val hasAppliedFilters = badgeCache.isActive
    private var loadJob: Job? = null

    init {
        retryLoading()
    }

    fun retryLoading() {
        loadJob?.cancel()
        _uiState.update { it.copy(isLoading = true, hasLoadError = false) }
        loadJob = viewModelScope.launch {
            try {
                val draft = if (savedStateHandle.get<Boolean>(DRAFT_INITIALIZED) == true) {
                    CatalogFilters(savedStateHandle[NAME_QUERY] ?: "", savedStateHandle[TYPE])
                } else repository.filters.first()
                setDraft(draft)
                _uiState.update { it.copy(isLoading = false) }
            } catch (error: CancellationException) {
                throw error
            } catch (_: IOException) {
                _uiState.update { it.copy(isLoading = false, hasLoadError = true) }
            }
        }
    }

    fun setNameQuery(value: String) = changeDraft(_uiState.value.draft.copy(nameQuery = value))

    fun setType(value: String?) = changeDraft(_uiState.value.draft.copy(type = value))

    fun reset() = changeDraft(CatalogFilters())

    fun apply() {
        val state = _uiState.value
        if (state.isLoading || state.isSaving || state.isSaved || state.hasLoadError) return
        _uiState.update { it.copy(isSaving = true, hasError = false) }
        viewModelScope.launch {
            try {
                repository.save(state.draft)
                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: IOException) {
                _uiState.update { it.copy(isSaving = false, hasError = true) }
            }
        }
    }

    private fun changeDraft(draft: CatalogFilters) {
        if (_uiState.value.isLoading || _uiState.value.isSaving || _uiState.value.hasLoadError) return
        setDraft(draft)
    }

    private fun setDraft(draft: CatalogFilters) {
        savedStateHandle[DRAFT_INITIALIZED] = true
        savedStateHandle[NAME_QUERY] = draft.nameQuery
        savedStateHandle[TYPE] = draft.type
        _uiState.update { it.copy(draft = draft, hasError = false) }
    }

    companion object {
        private const val DRAFT_INITIALIZED = "filters.draft_initialized"
        private const val NAME_QUERY = "filters.name_query"
        private const val TYPE = "filters.type"

        fun factory(repository: CatalogFiltersRepository, badgeCache: FilterBadgeCache) = viewModelFactory {
            initializer { FiltersViewModel(repository, createSavedStateHandle(), badgeCache) }
        }
    }
}
