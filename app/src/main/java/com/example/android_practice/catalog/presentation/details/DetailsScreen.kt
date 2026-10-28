package com.example.android_practice.catalog.presentation.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.android_practice.R
import com.example.android_practice.catalog.domain.model.PokemonDetails
import com.example.android_practice.catalog.presentation.components.ErrorStatus
import com.example.android_practice.catalog.presentation.components.LoadingStatus
import com.example.android_practice.catalog.presentation.components.PokemonArtwork
import com.example.android_practice.catalog.presentation.components.RefreshButton
import com.example.android_practice.catalog.presentation.components.displayName
import java.util.Locale

@Composable
fun DetailsScreen(
    uiState: DetailsUiState,
    favoriteState: FavoriteActionState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            IconButton(onClick = onBack) {
                Icon(painterResource(R.drawable.ic_back), contentDescription = stringResource(R.string.back))
            }
            Text(stringResource(R.string.details_title), style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f))
            IconButton(
                onClick = onToggleFavorite,
                enabled = uiState is DetailsUiState.Content && !favoriteState.isChanging &&
                    (favoriteState.isFavorite != null || favoriteState.hasError),
            ) {
                Icon(
                    painterResource(if (favoriteState.isFavorite == true) R.drawable.ic_favorite else R.drawable.ic_favorite_outline),
                    contentDescription = stringResource(if (favoriteState.isFavorite == true)
                        R.string.remove_favorite else R.string.add_favorite),
                    tint = if (favoriteState.isFavorite == true) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            RefreshButton(enabled = uiState !is DetailsUiState.Loading && !favoriteState.isChanging, onClick = onRefresh)
        }
        if (favoriteState.isChanging) Text(stringResource(R.string.updating_favorite), modifier = Modifier.padding(horizontal = 20.dp))
        if (favoriteState.hasError) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(stringResource(R.string.favorite_action_error), color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
                TextButton(onClick = onToggleFavorite, enabled = !favoriteState.isChanging) { Text(stringResource(R.string.retry)) }
            }
        }
        when (uiState) {
            DetailsUiState.Loading -> LoadingStatus(stringResource(R.string.loading_pokemon))
            is DetailsUiState.Error -> ErrorStatus(uiState.error, onRetry)
            is DetailsUiState.Content -> PokemonDetailsContent(uiState.pokemon)
        }
    }
}

@Composable
private fun PokemonDetailsContent(pokemon: PokemonDetails) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            PokemonArtwork(pokemon.name, pokemon.imageUrl, Modifier.size(120.dp), pokemon.localImagePath)
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.pokemon_number, pokemon.id), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(pokemon.name.displayName(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(pokemon.types.joinToString(" · ") { it.displayName() }, style = MaterialTheme.typography.bodyLarge)
        }
        Spacer(Modifier.height(24.dp))
        Card(Modifier.fillMaxWidth()) {
            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Fact(stringResource(R.string.height), stringResource(R.string.height_value,
                    String.format(Locale.US, "%.1f", pokemon.heightDecimeters / 10.0)))
                Fact(stringResource(R.string.weight), stringResource(R.string.weight_value,
                    String.format(Locale.US, "%.1f", pokemon.weightHectograms / 10.0)))
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(stringResource(R.string.abilities), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        pokemon.abilities.forEach { ability ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text(ability.name.displayName(), modifier = Modifier.weight(1f))
                if (ability.isHidden) {
                    Text(stringResource(R.string.hidden_ability), style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(stringResource(R.string.base_stats), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        with(pokemon.stats) {
            StatRow(stringResource(R.string.stat_hp), hp)
            StatRow(stringResource(R.string.stat_attack), attack)
            StatRow(stringResource(R.string.stat_defense), defense)
            StatRow(stringResource(R.string.stat_special_attack), specialAttack)
            StatRow(stringResource(R.string.stat_special_defense), specialDefense)
            StatRow(stringResource(R.string.stat_speed), speed)
        }
    }
}

@Composable
private fun Fact(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun StatRow(label: String, value: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(label, modifier = Modifier.width(100.dp), style = MaterialTheme.typography.bodyMedium)
        Text(value.toString(), modifier = Modifier.width(40.dp), fontWeight = FontWeight.SemiBold)
        LinearProgressIndicator(progress = { (value / 255f).coerceIn(0f, 1f) }, modifier = Modifier.weight(1f))
    }
}
