package com.example.android_practice.catalog.domain.model

enum class CatalogError {
    NETWORK,
    NOT_FOUND,
    REMOTE,
    INVALID_DATA,
    STORAGE,
}

class CatalogException(
    val error: CatalogError,
    cause: Throwable,
) : Exception(error.name, cause)
