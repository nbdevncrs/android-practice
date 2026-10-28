package com.example.android_practice.catalog.presentation.filters

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.android_practice.R
import com.example.android_practice.catalog.domain.model.CatalogError
import com.example.android_practice.catalog.domain.model.PokemonType
import com.example.android_practice.catalog.presentation.components.ErrorStatus
import com.example.android_practice.catalog.presentation.components.LoadingStatus
import com.example.android_practice.catalog.presentation.components.displayName

@Composable
fun FiltersScreen(
    state: FiltersUiState,
    hasAppliedFilters: Boolean,
    onBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onTypeChange: (String?) -> Unit,
    onReset: () -> Unit,
    onApply: () -> Unit,
    onRetryLoading: () -> Unit,
) {
    val enabled = !state.isLoading && !state.isSaving && !state.hasLoadError
    BackHandler(enabled = state.isSaving) { }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(8.dp)) {
            IconButton(onClick = onBack, enabled = !state.isSaving) {
                Icon(painterResource(R.drawable.ic_back), stringResource(R.string.back_to_catalog))
            }
            Text(stringResource(R.string.filters_title), style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f).padding(8.dp))
            TextButton(onClick = onReset, enabled = enabled) { Text(stringResource(R.string.reset_filters)) }
        }
        if (state.isLoading) {
            LoadingStatus(stringResource(R.string.loading_filters))
        } else if (state.hasLoadError) {
            ErrorStatus(CatalogError.STORAGE, onRetryLoading)
        } else {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Text(stringResource(if (hasAppliedFilters) R.string.filters_applied else R.string.filters_default),
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(
                    value = state.draft.nameQuery,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(R.string.pokemon_name)) },
                    singleLine = true,
                    enabled = enabled,
                    modifier = Modifier.fillMaxWidth(),
                )
                var expanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(onClick = { expanded = true }, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.pokemon_type) + ": " +
                            (state.draft.type?.displayName() ?: stringResource(R.string.all_types)))
                    }
                    DropdownMenu(expanded, onDismissRequest = { expanded = false }, modifier = Modifier.heightIn(max = 320.dp)) {
                        DropdownMenuItem(text = { Text(stringResource(R.string.all_types)) }, onClick = {
                            onTypeChange(null)
                            expanded = false
                        })
                        PokemonType.entries.forEach { type ->
                            DropdownMenuItem(text = { Text(type.apiName.displayName()) }, onClick = {
                                onTypeChange(type.apiName)
                                expanded = false
                            })
                        }
                    }
                }
                Text(stringResource(R.string.filters_apply_hint), style = MaterialTheme.typography.bodyMedium)
                if (state.hasError) Text(stringResource(R.string.filters_save_error), color = MaterialTheme.colorScheme.error)
                Button(onClick = onApply, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(if (state.isSaving) R.string.saving_filters else R.string.apply_filters))
                }
            }
        }
    }
}
