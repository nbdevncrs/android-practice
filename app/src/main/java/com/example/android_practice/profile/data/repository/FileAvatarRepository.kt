package com.example.android_practice.profile.data.repository

import android.content.Context
import android.graphics.BitmapFactory
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.example.android_practice.profile.domain.repository.AvatarRepository
import java.io.File
import java.io.IOException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

class FileAvatarRepository(
    context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : AvatarRepository {
    private val applicationContext = context.applicationContext
    private val avatars = File(applicationContext.filesDir, "profile/avatars")
    private val camera = File(applicationContext.cacheDir, "profile/camera")
    private val authority = "${applicationContext.packageName}.files"

    override suspend fun importPhoto(sourceUri: String): String = withContext(ioDispatcher) {
        val source = sourceUri.toUri()
        if (source.scheme != "content" && source.scheme != "file") throw IOException("Unsupported photo URI")
        ensureDirectory(avatars)
        val target = File.createTempFile("avatar-", ".img", avatars)
        try {
            val input = applicationContext.contentResolver.openInputStream(source)
                ?: throw IOException("Cannot open photo")
            input.use {
                target.outputStream().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var size = 0L
                    while (true) {
                        currentCoroutineContext().ensureActive()
                        val read = it.read(buffer)
                        if (read < 0) break
                        size += read
                        if (size > MAX_PHOTO_BYTES) throw IOException("Photo exceeds size limit")
                        output.write(buffer, 0, read)
                    }
                }
            }
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(target.absolutePath, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) throw IOException("Unsupported image")
            currentCoroutineContext().ensureActive()
            target.toUri().toString()
        } catch (error: Exception) {
            target.delete()
            if (error is SecurityException) throw IOException("Photo access denied", error)
            throw error
        }
    }

    override suspend fun createCameraUri(): String = withContext(ioDispatcher) {
        ensureDirectory(camera)
        val target = File.createTempFile("capture-", ".jpg", camera)
        try {
            FileProvider.getUriForFile(applicationContext, authority, target).toString()
        } catch (error: Exception) {
            target.delete()
            throw error
        }
    }

    override suspend fun discard(uri: String?) = withContext(ioDispatcher) {
        if (uri == null) return@withContext
        val parsed = uri.toUri()
        val target = when {
            parsed.scheme == "file" -> parsed.path?.let(::File)?.takeIf { it.canonicalFile.parentFile == avatars.canonicalFile }
            parsed.scheme == "content" && parsed.authority == authority &&
                parsed.pathSegments.firstOrNull() == "profile_camera" -> parsed.lastPathSegment?.let { name ->
                    File(camera, name).takeIf { it.canonicalFile.parentFile == camera.canonicalFile }
                }
            else -> null
        }
        if (target != null && target.exists() && !target.delete()) throw IOException("Cannot delete temporary photo")
    }

    private fun ensureDirectory(directory: File) {
        if (!directory.isDirectory && !directory.mkdirs()) throw IOException("Cannot create photo directory")
    }

    private companion object {
        const val MAX_PHOTO_BYTES = 20L * 1024 * 1024
    }
}
