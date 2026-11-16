package com.example.android_practice.profile.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.android_practice.R
import com.example.android_practice.profile.domain.model.ClassTime
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassTimePickerDialog(value: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    val initial = remember {
        val time = ClassTime.parse(value)
        val now = Calendar.getInstance()
        (time?.hour ?: now.get(Calendar.HOUR_OF_DAY)) to (time?.minute ?: now.get(Calendar.MINUTE))
    }
    val state = rememberTimePickerState(initialHour = initial.first, initialMinute = initial.second, is24Hour = true)

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.padding(16.dp).widthIn(max = 360.dp), shape = MaterialTheme.shapes.extraLarge) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Text(stringResource(R.string.profile_class_time), style = MaterialTheme.typography.titleLarge)
                TimePicker(state = state)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
                    TextButton(onClick = {
                        onConfirm("${state.hour.toString().padStart(2, '0')}:${state.minute.toString().padStart(2, '0')}")
                    }) { Text(stringResource(R.string.done)) }
                }
            }
        }
    }
}
