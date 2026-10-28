package com.example.android_practice.catalog.domain.repository

import com.example.android_practice.catalog.domain.model.CatalogFilters
import kotlinx.coroutines.flow.Flow

interface CatalogFiltersRepository {
    val filters: Flow<CatalogFilters>

    suspend fun save(filters: CatalogFilters)

    suspend fun reset()
}
