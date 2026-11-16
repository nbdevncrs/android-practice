package com.example.android_practice.profile.presentation.edit

import com.example.android_practice.profile.domain.repository.AvatarRepository

import java.io.IOException


internal class TestAvatarRepository : AvatarRepository {
    val discarded = mutableListOf<String>()
    var importError: IOException? = null
    var imports = 0
    var cameraRequests = 0

    override suspend fun importPhoto(sourceUri: String): String {
        importError?.let { throw it }
        imports++
        return "file:///private/avatar-$imports.img"
    }

    override suspend fun createCameraUri(): String {
        cameraRequests++
        return "content://app.files/profile_camera/capture-$cameraRequests.jpg"
    }

    override suspend fun discard(uri: String?) {
        if (uri != null) discarded += uri
    }
}
