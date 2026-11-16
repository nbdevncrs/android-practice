package com.example.android_practice.profile.domain.repository

interface AvatarRepository {
    suspend fun importPhoto(sourceUri: String): String
    suspend fun createCameraUri(): String
    suspend fun discard(uri: String?)
}
