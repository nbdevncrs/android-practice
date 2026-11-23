package com.example.android_practice.profile.presentation.edit

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.android_practice.profile.presentation.R
import com.example.android_practice.profile.presentation.components.ProfileAvatar
import com.example.android_practice.profile.presentation.components.ClassTimePickerDialog
import com.example.android_practice.profile.domain.model.ClassTimeValidation

@Composable
internal fun EditProfileScreen(
    state: EditProfileUiState,
    onBack: () -> Unit,
    onFullNameChange: (String) -> Unit,
    onResumeUrlChange: (String) -> Unit,
    onFavoriteClassTimeChange: (String) -> Unit,
    onDone: () -> Unit,
    onReminderSettings: () -> Unit,
    onRetryLoading: () -> Unit,
    onGallery: () -> Unit,
    onCamera: () -> Unit,
) {
    var showPhotoSource by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    if (showTimePicker && state.canEdit) {
        ClassTimePickerDialog(
            value = state.draft.favoriteClassTime,
            onConfirm = { value ->
                onFavoriteClassTimeChange(value)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false },
        )
    }
    BackHandler(enabled = state.isBusy) { }
    if (showPhotoSource) {
        AlertDialog(
            onDismissRequest = { showPhotoSource = false },
            title = { Text(stringResource(R.string.profile_choose_photo)) },
            text = {
                Column {
                    TextButton(onClick = { showPhotoSource = false; onGallery() }) { Text(stringResource(R.string.profile_gallery)) }
                    TextButton(onClick = { showPhotoSource = false; onCamera() }) { Text(stringResource(R.string.profile_camera)) }
                }
            },
            confirmButton = { TextButton(onClick = { showPhotoSource = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }
    Column(Modifier.fillMaxSize().imePadding()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            IconButton(onClick = onBack, enabled = !state.isBusy) {
                Icon(painterResource(R.drawable.ic_back), contentDescription = stringResource(R.string.back))
            }
            Text(stringResource(R.string.edit_profile), style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f))
            TextButton(onClick = onDone, enabled = state.canSave) {
                Text(stringResource(if (state.isSaving) R.string.profile_saving else R.string.done))
            }
        }
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.error == EditProfileError.LOAD -> Column(
                Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            ) {
                Text(stringResource(R.string.profile_load_error), color = MaterialTheme.colorScheme.error)
                Button(onClick = onRetryLoading) { Text(stringResource(R.string.retry)) }
            }
            else -> Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.clickable(enabled = state.canEdit, onClickLabel = stringResource(R.string.profile_change_photo)) {
                        showPhotoSource = true
                    }) {
                        ProfileAvatar(state.draft.avatarUri, Modifier.size(128.dp))
                    }
                    TextButton(onClick = { showPhotoSource = true }, enabled = state.canEdit) {
                        Text(stringResource(R.string.profile_change_photo))
                    }
                    if (state.isImportingAvatar) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    if (state.error == EditProfileError.AVATAR || state.error == EditProfileError.CAMERA) {
                        Text(stringResource(if (state.error == EditProfileError.CAMERA)
                            R.string.profile_camera_error else R.string.profile_photo_error), color = MaterialTheme.colorScheme.error)
                    }
                }
                OutlinedTextField(
                    value = state.draft.fullName,
                    onValueChange = onFullNameChange,
                    label = { Text(stringResource(R.string.profile_full_name)) },
                    enabled = state.canEdit,
                    singleLine = true,
                    isError = state.validation.fullNameRequired,
                    supportingText = {
                        if (state.validation.fullNameRequired) Text(stringResource(R.string.profile_name_required))
                    },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = state.draft.resumeUrl,
                    onValueChange = onResumeUrlChange,
                    label = { Text(stringResource(R.string.profile_resume_url)) },
                    enabled = state.canEdit,
                    singleLine = true,
                    isError = state.validation.invalidResumeUrl,
                    supportingText = {
                        Text(stringResource(if (state.validation.invalidResumeUrl)
                            R.string.profile_invalid_resume_url else R.string.profile_resume_optional))
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = state.draft.favoriteClassTime,
                    onValueChange = onFavoriteClassTimeChange,
                    label = { Text(stringResource(R.string.profile_class_time)) },
                    placeholder = { Text(stringResource(R.string.profile_class_time_format)) },
                    enabled = state.canEdit,
                    singleLine = true,
                    isError = state.timeValidation != ClassTimeValidation.VALID,
                    supportingText = {
                        Text(stringResource(when (state.timeValidation) {
                            ClassTimeValidation.REQUIRED -> R.string.profile_class_time_required
                            ClassTimeValidation.INVALID_FORMAT -> R.string.profile_class_time_invalid
                            ClassTimeValidation.VALID -> R.string.profile_class_time_hint
                        }))
                    },
                    trailingIcon = {
                        IconButton(onClick = { showTimePicker = true }, enabled = state.canEdit) {
                            Icon(painterResource(R.drawable.ic_clock),
                                contentDescription = stringResource(R.string.profile_choose_class_time))
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { if (state.canSave) onDone() }),
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(stringResource(R.string.profile_edit_hint), style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (state.error == EditProfileError.SAVE) {
                    Text(stringResource(R.string.profile_save_error), color = MaterialTheme.colorScheme.error)
                    Button(onClick = onDone, enabled = state.canSave) { Text(stringResource(R.string.retry)) }
                }
                if (state.isProfileSavedWithoutReminder) {
                    Text(stringResource(R.string.reminder_profile_saved), color = MaterialTheme.colorScheme.error)
                }
                if (state.error == EditProfileError.TIME_PASSED) {
                    Text(stringResource(R.string.reminder_time_passed), color = MaterialTheme.colorScheme.error)
                }
                if (state.error == EditProfileError.REMINDER) {
                    Text(stringResource(R.string.reminder_schedule_error), color = MaterialTheme.colorScheme.error)
                    Button(onClick = onDone, enabled = state.canSave) { Text(stringResource(R.string.retry)) }
                }
                if (state.error == EditProfileError.NOTIFICATIONS || state.error == EditProfileError.EXACT_ALARM ||
                    state.error == EditProfileError.SETTINGS) {
                    Text(stringResource(when (state.error) {
                        EditProfileError.NOTIFICATIONS -> R.string.reminder_notifications_required
                        EditProfileError.EXACT_ALARM -> R.string.reminder_exact_alarm_required
                        else -> R.string.reminder_settings_unavailable
                    }), color = MaterialTheme.colorScheme.error)
                    Button(onClick = onReminderSettings, enabled = state.canEdit) {
                        Text(stringResource(R.string.reminder_open_settings))
                    }
                }
            }
        }
    }
}
