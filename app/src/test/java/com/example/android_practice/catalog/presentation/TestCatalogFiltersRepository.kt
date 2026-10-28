package com.example.android_practice.catalog.presentation

import com.example.android_practice.catalog.domain.model.CatalogFilters
import com.example.android_practice.catalog.domain.repository.CatalogFiltersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class TestCatalogFiltersRepository(initial: CatalogFilters = CatalogFilters()) : CatalogFiltersRepository {
    private val values = MutableStateFlow(initial)
    var readError: java.io.IOException? = null
    val currentFilters get() = values.value
    override val filters: Flow<CatalogFilters> = flow {
        readError?.let { throw it }
        values.collect { emit(it) }
    }
    var saveError: java.io.IOException? = null

    override suspend fun save(filters: CatalogFilters) {
        saveError?.let { throw it }
        values.value = filters.normalized()
    }

    override suspend fun reset() = save(CatalogFilters())
}
