package com.example.android_practice.catalog.presentation.filters

import com.example.android_practice.catalog.domain.repository.CatalogFiltersRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.delay
import java.io.IOException

class FilterBadgeCache(
    repository: CatalogFiltersRepository,
    scope: CoroutineScope,
) {
    val isActive = repository.filters
        .retryWhen { error, _ ->
            if (error is IOException) {
                delay(1_000)
                true
            } else false
        }
        .map { it.isActive }
        .stateIn(scope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000), false)
}
