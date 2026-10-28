package com.example.android_practice.profile.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.android_practice.R
import com.example.android_practice.profile.domain.model.UserProfile
import com.example.android_practice.profile.presentation.components.ProfileAvatar

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onRetry: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onResume: (() -> Unit)? = null,
    resumeState: ResumeUiState = ResumeUiState.Idle,
    onCancelResume: () -> Unit = {},
) {
    Column(Modifier.fillMaxSize()) {
        Text(stringResource(R.string.profile_title), style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(20.dp))
        when (state) {
            ProfileUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            ProfileUiState.Error -> Column(
                Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            ) {
                Text(stringResource(R.string.profile_load_error), textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error)
                Button(onClick = onRetry) { Text(stringResource(R.string.retry)) }
            }
            is ProfileUiState.Content -> ProfileContent(state.profile, onEdit, onResume, resumeState, onCancelResume)
        }
    }
}

@Composable
private fun ProfileContent(
    profile: UserProfile,
    onEdit: (() -> Unit)?,
    onResume: (() -> Unit)?,
    resumeState: ResumeUiState,
    onCancelResume: () -> Unit,
) {
    val downloading = resumeState is ResumeUiState.Downloading
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        ProfileAvatar(profile.avatarUri, Modifier.size(128.dp))
        Text(profile.fullName.ifBlank { stringResource(R.string.profile_no_name) },
            style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        if (profile == UserProfile()) {
            Text(stringResource(R.string.profile_empty_hint), color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center)
        }
        OutlinedButton(onClick = { onEdit?.invoke() }, enabled = onEdit != null && !downloading, modifier = Modifier.fillMaxWidth()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(R.drawable.ic_edit), contentDescription = null, modifier = Modifier.size(20.dp))
                Text(stringResource(R.string.edit_profile))
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.profile_resume), style = MaterialTheme.typography.titleLarge)
                Text(profile.resumeUrl.ifBlank { stringResource(R.string.profile_no_resume) },
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(
                    onClick = { onResume?.invoke() },
                    enabled = profile.resumeUrl.isNotBlank() && onResume != null && !downloading,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(if (downloading) R.string.resume_downloading else R.string.profile_open_resume))
                }
                if (downloading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    TextButton(onClick = onCancelResume) { Text(stringResource(R.string.cancel)) }
                }
                if (resumeState is ResumeUiState.Error) {
                    val message = when (resumeState.reason) {
                        ResumeUiError.INVALID_URL -> R.string.profile_invalid_resume_url
                        ResumeUiError.NETWORK -> R.string.resume_network_error
                        ResumeUiError.SERVER -> R.string.resume_server_error
                        ResumeUiError.INVALID_FILE -> R.string.resume_file_error
                        ResumeUiError.STORAGE -> R.string.resume_storage_error
                        ResumeUiError.NO_VIEWER -> R.string.resume_no_viewer
                    }
                    Text(stringResource(message), color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
