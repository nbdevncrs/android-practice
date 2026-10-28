package com.example.android_practice.catalog.presentation.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.android_practice.R
import com.example.android_practice.catalog.presentation.components.ErrorStatus
import com.example.android_practice.catalog.presentation.components.LoadingStatus
import com.example.android_practice.catalog.presentation.components.PokemonCard
import com.example.android_practice.catalog.presentation.components.RefreshButton

@Composable
fun CatalogScreen(
    uiState: CatalogUiState,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onPokemonClick: (Int) -> Unit,
    hasActiveFilters: Boolean,
    onFiltersClick: () -> Unit,
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
            IconButton(onClick = onFiltersClick) {
                BadgedBox(badge = { if (hasActiveFilters) Badge() }) {
                    Icon(painterResource(R.drawable.ic_filters), contentDescription = stringResource(
                        if (hasActiveFilters) R.string.filters_active_description else R.string.filters_title))
                }
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
                                PokemonCard(item, onClick = { onPokemonClick(item.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}

