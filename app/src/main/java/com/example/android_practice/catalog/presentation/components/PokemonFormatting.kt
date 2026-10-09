package com.example.android_practice.catalog.presentation.components

internal fun String.displayName(): String = replace('-', ' ').replaceFirstChar { it.uppercase() }
