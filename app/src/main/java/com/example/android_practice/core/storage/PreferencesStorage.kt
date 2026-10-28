package com.example.android_practice.core.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

private val Context.appPreferences: DataStore<Preferences> by preferencesDataStore(
    name = "app_preferences",
)

internal fun applicationPreferences(context: Context): DataStore<Preferences> =
    context.applicationContext.appPreferences
