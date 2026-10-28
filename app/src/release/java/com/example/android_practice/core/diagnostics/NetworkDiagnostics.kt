package com.example.android_practice.core.diagnostics

import android.content.Context
import okhttp3.OkHttpClient

@Suppress("UNUSED_PARAMETER")
internal fun OkHttpClient.Builder.withNetworkDiagnostics(context: Context): OkHttpClient.Builder = this
