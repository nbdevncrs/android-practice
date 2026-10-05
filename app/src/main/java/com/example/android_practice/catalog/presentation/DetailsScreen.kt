package com.example.android_practice.catalog.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.android_practice.catalog.domain.PokemonDetails

@Composable
fun DetailsScreen(pokemon: PokemonDetails?, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Button(onClick = onBack) { Text("← Back to catalog") }
        Spacer(Modifier.height(18.dp))
        if (pokemon == null) {
            Text("Pokémon not found", style = MaterialTheme.typography.titleLarge)
            return@Column
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            PokemonArtwork(pokemon.name, Modifier.size(118.dp))
            Column(Modifier.padding(start = 20.dp)) {
                Text("№ ${pokemon.id.toString().padStart(3, '0')}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(pokemon.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(pokemon.types.joinToString(" · ") { it.displayName })
            }
        }
        Spacer(Modifier.height(24.dp))
        Card(Modifier.fillMaxWidth()) {
            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Fact("Height", "${pokemon.heightDecimeters / 10.0} m")
                Fact("Weight", "${pokemon.weightHectograms / 10.0} kg")
            }
        }
        Spacer(Modifier.height(24.dp))
        Text("Abilities", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(pokemon.abilities.joinToString(" · "), style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(24.dp))
        Text("Base stats", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        with(pokemon.stats) {
            StatRow("HP", hp)
            StatRow("Attack", attack)
            StatRow("Defense", defense)
            StatRow("Sp. Attack", specialAttack)
            StatRow("Sp. Defense", specialDefense)
            StatRow("Speed", speed)
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
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Text(value.toString(), modifier = Modifier.weight(0.35f), fontWeight = FontWeight.SemiBold)
        LinearProgressIndicator(progress = { (value / 150f).coerceIn(0f, 1f) }, modifier = Modifier.weight(1.1f))
    }
}
