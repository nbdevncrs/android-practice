package com.example.android_practice.catalog.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.android_practice.catalog.domain.model.CatalogFilters
import com.example.android_practice.catalog.domain.repository.CatalogFiltersRepository
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class DataStoreCatalogFiltersRepository(
    private val preferences: DataStore<Preferences>,
) : CatalogFiltersRepository {
    override val filters = preferences.data
        .map { values ->
            CatalogFilters(
                nameQuery = values[NAME_QUERY].orEmpty(),
                type = values[TYPE],
            ).normalized()
        }
        .distinctUntilChanged()

    override suspend fun save(filters: CatalogFilters) {
        val normalized = filters.normalized()
        preferences.edit { values ->
            if (normalized.nameQuery.isEmpty()) values.remove(NAME_QUERY)
            else values[NAME_QUERY] = normalized.nameQuery

            val type = normalized.type
            if (type == null) values.remove(TYPE) else values[TYPE] = type
        }
    }

    override suspend fun reset() = save(CatalogFilters())

    private companion object {
        val NAME_QUERY = stringPreferencesKey("catalog.filters.name_query")
        val TYPE = stringPreferencesKey("catalog.filters.type")
    }
}
