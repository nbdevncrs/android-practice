package com.example.android_practice.core.diagnostics

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.chuckerteam.chucker.api.Chucker
import com.example.android_practice.R

@Composable
fun NetworkDiagnosticsButton() {
    val context = LocalContext.current
    FloatingActionButton(onClick = { context.startActivity(Chucker.getLaunchIntent(context)) }) {
        Text(stringResource(R.string.network_requests))
    }
}
