package com.example.android_practice.profile.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.android_practice.profile.domain.model.UserProfile
import com.example.android_practice.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class DataStoreProfileRepository(
    private val preferences: DataStore<Preferences>,
) : ProfileRepository {
    override val profile = preferences.data
        .map { values ->
            UserProfile(
                fullName = values[FULL_NAME].orEmpty(),
                avatarUri = values[AVATAR_URI],
                resumeUrl = values[RESUME_URL].orEmpty(),
            ).normalized()
        }
        .distinctUntilChanged()

    override suspend fun save(profile: UserProfile) {
        val normalized = profile.normalized()
        preferences.edit { values ->
            values.setOrRemove(FULL_NAME, normalized.fullName)
            values.setOrRemove(AVATAR_URI, normalized.avatarUri)
            values.setOrRemove(RESUME_URL, normalized.resumeUrl)
        }
    }

    private fun MutablePreferences.setOrRemove(key: Preferences.Key<String>, value: String?) {
        if (value.isNullOrEmpty()) remove(key) else this[key] = value
    }

    private companion object {
        val FULL_NAME = stringPreferencesKey("profile.full_name")
        val AVATAR_URI = stringPreferencesKey("profile.avatar_uri")
        val RESUME_URL = stringPreferencesKey("profile.resume_url")
    }
}
