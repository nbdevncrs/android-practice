package com.example.android_practice.catalog.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun PokemonArtwork(name: String, imageUrl: String?, modifier: Modifier = Modifier) {
    val artworkUrl = imageUrl?.takeIf { it.isNotBlank() }
    var showPlaceholder by remember(artworkUrl) { mutableStateOf(true) }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
            .clearAndSetSemantics { },
    ) {
        if (showPlaceholder) {
            Text(name.take(1).uppercase(), style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
        if (artworkUrl != null) {
            AsyncImage(
                model = artworkUrl,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                onLoading = { showPlaceholder = true },
                onSuccess = { showPlaceholder = false },
                onError = { showPlaceholder = true },
                modifier = Modifier.matchParentSize().padding(6.dp),
            )
        }
    }
}

