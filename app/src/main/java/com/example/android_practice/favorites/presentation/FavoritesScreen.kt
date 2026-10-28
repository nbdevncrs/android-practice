package com.example.android_practice.favorites.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.android_practice.R
import com.example.android_practice.catalog.domain.model.CatalogError
import com.example.android_practice.catalog.presentation.components.ErrorStatus
import com.example.android_practice.catalog.presentation.components.LoadingStatus
import com.example.android_practice.catalog.presentation.components.PokemonCard

@Composable
fun FavoritesScreen(state: FavoritesUiState, onRetry: () -> Unit, onPokemonClick: (Int) -> Unit) {
    val listState = rememberLazyListState()
    Column(Modifier.fillMaxSize()) {
        Text(stringResource(R.string.favorites_title), style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(20.dp))
        when (state) {
            FavoritesUiState.Loading -> LoadingStatus(stringResource(R.string.loading_favorites))
            FavoritesUiState.Error -> ErrorStatus(CatalogError.STORAGE, onRetry)
            is FavoritesUiState.Content -> {
                if (state.pokemon.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.empty_favorites), modifier = Modifier.padding(24.dp))
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(16.dp),
                    ) {
                        items(state.pokemon, key = { it.id }) { pokemon ->
                            PokemonCard(pokemon, onClick = { onPokemonClick(pokemon.id) })
                        }
                    }
                }
            }
        }
    }
}
