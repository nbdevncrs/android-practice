package com.example.android_practice.profile.domain.usecase

import com.example.android_practice.profile.domain.repository.AvatarRepository
import com.example.android_practice.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.first

class DiscardUnusedAvatar(
    private val profiles: ProfileRepository,
    private val avatars: AvatarRepository,
) {
    suspend operator fun invoke(uri: String?) {
        if (uri == null || profiles.profile.first().avatarUri == uri) return
        avatars.discard(uri)
    }
}
