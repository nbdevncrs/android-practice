package com.example.android_practice.profile.presentation.platform

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.example.android_practice.profile.presentation.edit.EditProfileError
import com.example.android_practice.reminders.domain.repository.ReminderAccess

internal class ReminderAccessRequest(
    val isPending: Boolean,
    val request: () -> Unit,
    val openSettings: () -> Unit,
)

@Composable
internal fun rememberReminderAccessRequest(
    access: ReminderAccess,
    onGranted: () -> Unit,
    onDenied: (EditProfileError) -> Unit,
): ReminderAccessRequest {
    val context = LocalContext.current
    var pending by rememberSaveable { mutableStateOf(false) }
    val settings = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        pending = false
        when {
            !access.notificationsAllowed() -> onDenied(EditProfileError.NOTIFICATIONS)
            !access.exactAlarmsAllowed() -> onDenied(EditProfileError.EXACT_ALARM)
            else -> onGranted()
        }
    }

    fun launchSettings(intent: Intent) {
        pending = true
        try {
            settings.launch(intent)
        } catch (_: ActivityNotFoundException) {
            pending = false
            onDenied(EditProfileError.SETTINGS)
        } catch (_: SecurityException) {
            pending = false
            onDenied(EditProfileError.SETTINGS)
        }
    }

    fun requestExactAlarm() {
        if (access.exactAlarmsAllowed()) {
            pending = false
            onGranted()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            launchSettings(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                "package:${context.packageName}".toUri()))
        } else {
            pending = false
            onDenied(EditProfileError.SETTINGS)
        }
    }

    val notifications = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted && access.notificationsAllowed()) {
            requestExactAlarm()
        } else {
            pending = false
            onDenied(EditProfileError.NOTIFICATIONS)
        }
    }

    return ReminderAccessRequest(
        isPending = pending,
        request = {
            if (!pending) {
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED -> {
                        pending = true
                        try {
                            notifications.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } catch (_: ActivityNotFoundException) {
                            pending = false
                            onDenied(EditProfileError.SETTINGS)
                        } catch (_: SecurityException) {
                            pending = false
                            onDenied(EditProfileError.SETTINGS)
                        }
                    }
                    !access.notificationsAllowed() -> onDenied(EditProfileError.NOTIFICATIONS)
                    else -> requestExactAlarm()
                }
            }
        },
        openSettings = {
            if (!pending) {
                if (!access.notificationsAllowed()) {
                    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    } else {
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:${context.packageName}".toUri())
                    }
                    launchSettings(intent)
                } else {
                    requestExactAlarm()
                }
            }
        },
    )
}

