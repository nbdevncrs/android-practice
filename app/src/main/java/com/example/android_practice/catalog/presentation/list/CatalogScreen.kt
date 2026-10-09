package com.example.android_practice.catalog.presentation.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.android_practice.R
import com.example.android_practice.catalog.domain.model.PokemonSummary
import com.example.android_practice.catalog.presentation.components.ErrorStatus
import com.example.android_practice.catalog.presentation.components.LoadingStatus
import com.example.android_practice.catalog.presentation.components.PokemonArtwork
import com.example.android_practice.catalog.presentation.components.RefreshButton
import com.example.android_practice.catalog.presentation.components.displayName

@Composable
fun CatalogScreen(
    uiState: CatalogUiState,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onPokemonClick: (Int) -> Unit,
) {
    val listState = rememberLazyListState()
    Column(Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(20.dp)) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.catalog_title), style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.catalog_description), style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            RefreshButton(enabled = uiState !is CatalogUiState.Loading, onClick = onRefresh)
        }
        Box(Modifier.weight(1f).fillMaxWidth()) {
            when (uiState) {
                CatalogUiState.Loading -> LoadingStatus(stringResource(R.string.loading_catalog))
                is CatalogUiState.Error -> ErrorStatus(uiState.error, onRetry)
                is CatalogUiState.Content -> {
                    if (uiState.page.items.isEmpty()) {
                        Text(stringResource(R.string.empty_catalog), modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(
                            state = listState,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 20.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            items(uiState.page.items, key = { it.id }) { item ->
                                PokemonRow(item, onClick = { onPokemonClick(item.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PokemonRow(pokemon: PokemonSummary, onClick: () -> Unit) {
    Card(onClick = onClick, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            PokemonArtwork(pokemon.name, pokemon.imageUrl, Modifier.size(64.dp))
            Spacer(Modifier.size(16.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.pokemon_number, pokemon.id), style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(pokemon.name.displayName(), style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(pokemon.types.joinToString(" · ") { it.displayName() }, style = MaterialTheme.typography.bodyMedium)
            }
            Text("›", style = MaterialTheme.typography.headlineMedium)
        }
    }
}
