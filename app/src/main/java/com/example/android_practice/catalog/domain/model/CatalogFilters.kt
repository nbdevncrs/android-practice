package com.example.android_practice.catalog.domain.model

import java.util.Locale

data class CatalogFilters(
    val nameQuery: String = "",
    val type: String? = null,
) {
    val isActive: Boolean
        get() = nameQuery.isNotBlank() || !type.isNullOrBlank()

    fun normalized(): CatalogFilters = copy(
        nameQuery = nameQuery.trim().lowercase(Locale.ROOT),
        type = type?.trim()?.lowercase(Locale.ROOT)?.takeIf { it.isNotEmpty() },
    )
}
