package com.example.android_practice.profile.data

import androidx.core.net.toUri
import android.util.Base64
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.android_practice.profile.data.repository.FileAvatarRepository
import com.example.android_practice.profile.domain.model.UserProfile
import com.example.android_practice.profile.domain.repository.ProfileRepository
import com.example.android_practice.profile.domain.usecase.DiscardUnusedAvatar
import java.io.File
import java.io.FileNotFoundException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FileAvatarRepositoryTest {
    private val png = Base64.decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+jzfoAAAAASUVORK5CYII=", Base64.DEFAULT)

    @Test
    fun importedCopySurvivesSourceDeletionAndSavedAvatarCannotBeDiscarded() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val profiles = TestProfiles()
        val repository = FileAvatarRepository(context)
        val discard = DiscardUnusedAvatar(profiles, repository)
        val source = File.createTempFile("avatar-source-", ".png", context.cacheDir)
        var imported: String? = null
        try {
            source.writeBytes(png)
            imported = repository.importPhoto(source.toUri().toString())
            val copy = File(requireNotNull(imported.toUri().path))
            source.delete()
            assertArrayEquals(png, copy.readBytes())

            profiles.save(UserProfile("Alex", avatarUri = imported))
            discard(imported)
            assertTrue(copy.exists())
            profiles.save(UserProfile("Alex"))
            discard(imported)
            assertFalse(copy.exists())
        } finally {
            profiles.save(UserProfile())
            discard(imported)
            source.delete()
        }
    }

    @Test
    fun cameraUriAllowsWritingAndCleanupDoesNotTouchExternalFiles() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = FileAvatarRepository(context)
        val discard = DiscardUnusedAvatar(TestProfiles(), repository)
        val cameraUri = repository.createCameraUri()
        val unrelated = File.createTempFile("unrelated-", ".png", context.cacheDir)
        var imported: String? = null
        try {
            context.contentResolver.openOutputStream(cameraUri.toUri())!!.use { it.write(png) }
            imported = repository.importPhoto(cameraUri)
            discard(cameraUri)
            try {
                context.contentResolver.openInputStream(cameraUri.toUri())?.use { }
                fail("Temporary camera file should be deleted")
            } catch (_: FileNotFoundException) {
            }
            discard(unrelated.toUri().toString())
            assertTrue(unrelated.exists())
            assertArrayEquals(png, File(requireNotNull(imported.toUri().path)).readBytes())
        } finally {
            discard(cameraUri)
            discard(imported)
            unrelated.delete()
        }
    }

    private class TestProfiles : ProfileRepository {
        override val profile = MutableStateFlow(UserProfile())
        override suspend fun save(profile: UserProfile) {
            this.profile.value = profile
        }
    }
}
