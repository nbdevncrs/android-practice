package com.example.android_practice.core.diagnostics

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import okhttp3.OkHttpClient

internal fun OkHttpClient.Builder.withNetworkDiagnostics(context: Context): OkHttpClient.Builder =
    addInterceptor(ChuckerInterceptor.Builder(context.applicationContext).build())
