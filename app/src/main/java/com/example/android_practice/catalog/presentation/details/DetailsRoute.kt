package com.example.android_practice.catalog.presentation.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry

@Composable
fun DetailsRoute(entry: NavBackStackEntry, factory: ViewModelProvider.Factory, onBack: () -> Unit) {
    val vm: DetailsViewModel = viewModel(viewModelStoreOwner = entry, factory = factory)
    val state by vm.uiState.collectAsStateWithLifecycle()
    val favoriteState by vm.favoriteState.collectAsStateWithLifecycle()
    DetailsScreen(state, favoriteState, onBack = onBack, onRefresh = vm::refresh,
        onRetry = vm::retry, onToggleFavorite = vm::toggleFavorite)
}
