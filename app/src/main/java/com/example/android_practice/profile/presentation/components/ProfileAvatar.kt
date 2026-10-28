package com.example.android_practice.profile.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.android_practice.R

@Composable
internal fun ProfileAvatar(avatarUri: String?, modifier: Modifier = Modifier) {
    val uri = avatarUri?.takeIf { it.isNotBlank() }
    var showPlaceholder by remember(uri) { mutableStateOf(true) }
    val description = stringResource(R.string.profile_avatar)
    Box(
        modifier = modifier.clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        if (showPlaceholder) {
            Icon(painterResource(R.drawable.ic_profile), contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.fillMaxSize().padding(24.dp))
        }
        if (uri != null) {
            AsyncImage(
                model = uri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                onLoading = { showPlaceholder = true },
                onSuccess = { showPlaceholder = false },
                onError = { showPlaceholder = true },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
