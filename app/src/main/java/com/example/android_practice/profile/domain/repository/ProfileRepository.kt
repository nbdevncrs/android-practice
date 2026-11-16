package com.example.android_practice.profile.domain.repository

import com.example.android_practice.profile.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    val profile: Flow<UserProfile>

    suspend fun save(profile: UserProfile)
}
