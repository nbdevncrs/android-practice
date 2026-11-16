package com.example.android_practice.profile.presentation.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import com.example.android_practice.catalog.presentation.MainDispatcherRule
import com.example.android_practice.profile.domain.model.UserProfile
import com.example.android_practice.profile.domain.repository.ProfileRepository
import com.example.android_practice.profile.domain.repository.AvatarRepository
import com.example.android_practice.profile.domain.usecase.DiscardUnusedAvatar
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditProfileViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()
    private val store = ViewModelStore()
    private val cleanupScope = CoroutineScope(SupervisorJob() + dispatcherRule.dispatcher)

    @After
    fun tearDown() {
        store.clear()
        cleanupScope.cancel()
    }

    @Test
    fun changesRemainDraftUntilDoneAndSavedAvatarIsPreserved() = runTest {
        val original = UserProfile("Alex", "content://app/avatar", "https://example.com/old.pdf")
        val repository = TestRepository(original)
        val vm = createViewModel(repository)
        runCurrent()
        vm.setFullName(" Alex Morgan ")
        vm.setResumeUrl(" https://example.com/NewResume.pdf ")
        assertEquals(original, repository.current)

        vm.save()
        runCurrent()
        assertEquals(UserProfile("Alex Morgan", original.avatarUri, "https://example.com/NewResume.pdf"), repository.current)
        assertTrue(vm.uiState.value.isSaved)
    }

    @Test
    fun invalidFormIsNotSavedAndDiscardingDraftKeepsOriginalProfile() = runTest {
        val original = UserProfile("Alex")
        val repository = TestRepository(original)
        val vm = createViewModel(repository)
        runCurrent()
        vm.setFullName(" ")
        vm.setResumeUrl("not a URL")
        vm.save()
        runCurrent()
        assertTrue(vm.uiState.value.validation.fullNameRequired)
        assertTrue(vm.uiState.value.validation.invalidResumeUrl)
        assertEquals(0, repository.saveAttempts)

        store.clear()
        assertEquals(original, repository.current)
    }

    @Test
    fun draftSurvivesRecreationWithoutOverwritingSavedData() = runTest {
        val original = UserProfile("Alex", "content://app/avatar")
        val repository = TestRepository(original)
        val handle = SavedStateHandle()
        val first = createViewModel(repository, handle)
        runCurrent()
        first.setFullName("Unsaved name")
        first.setResumeUrl("https://example.com/resume.pdf")
        val restoredHandle = SavedStateHandle(handle.keys().associateWith { handle.get<Any?>(it) })
        store.clear()
        val restored = createViewModel(repository, restoredHandle)
        runCurrent()

        assertEquals(UserProfile("Unsaved name", original.avatarUri, "https://example.com/resume.pdf"), restored.uiState.value.draft)
        assertEquals(original, repository.current)
    }

    @Test
    fun readAndWriteErrorsAllowRetryWithoutLosingDraft() = runTest {
        val repository = TestRepository(UserProfile("Alex")).apply { readError = IOException("Read failed") }
        val vm = createViewModel(repository)
        runCurrent()
        assertEquals(EditProfileError.LOAD, vm.uiState.value.error)
        vm.save()
        assertEquals(0, repository.saveAttempts)

        repository.readError = null
        vm.retryLoading()
        runCurrent()
        vm.setFullName("Updated")
        repository.writeError = IOException("Write failed")
        vm.save()
        runCurrent()
        assertEquals(EditProfileError.SAVE, vm.uiState.value.error)
        assertEquals("Updated", vm.uiState.value.draft.fullName)
        assertEquals("Alex", repository.current.fullName)

        repository.writeError = null
        vm.save()
        runCurrent()
        assertEquals("Updated", repository.current.fullName)
        assertTrue(vm.uiState.value.isSaved)
    }

    @Test
    fun repeatedDoneAndEditsAreIgnoredWhileSaving() = runTest {
        val repository = TestRepository(UserProfile("Alex"))
        val gate = CompletableDeferred<Unit>()
        repository.saveGate = gate
        val vm = createViewModel(repository)
        runCurrent()
        vm.setFullName("Updated")
        vm.save()
        vm.save()
        vm.setFullName("Ignored")
        runCurrent()
        assertTrue(vm.uiState.value.isSaving)
        assertFalse(vm.uiState.value.canEdit)
        assertEquals(1, repository.saveAttempts)

        gate.complete(Unit)
        runCurrent()
        assertEquals("Updated", repository.current.fullName)
        assertTrue(vm.uiState.value.isSaved)
    }

    private fun createViewModel(
        repository: ProfileRepository,
        handle: SavedStateHandle = SavedStateHandle(),
        avatars: AvatarRepository = TestAvatarRepository(),
    ) = EditProfileViewModel(repository, handle, avatars, cleanupScope, DiscardUnusedAvatar(repository, avatars)).also { store.put("editProfile", it) }

    @Test
    fun discardedPhotoDraftDoesNotDeleteSavedAvatar() = runTest {
        val original = UserProfile("Alex", "file:///original.img")
        val repository = TestRepository(original)
        val avatars = TestAvatarRepository()
        val vm = createViewModel(repository, avatars = avatars)
        runCurrent()
        vm.selectPhoto("content://gallery/photo")
        runCurrent()
        val imported = vm.uiState.value.draft.avatarUri
        assertEquals(original, repository.current)
        assertFalse(avatars.discarded.contains(original.avatarUri))

        store.clear()
        runCurrent()
        assertTrue(avatars.discarded.contains(imported))
        assertEquals(original, repository.current)
    }

    @Test
    fun savedNewAvatarReplacesOldFileAndIsKeptAfterLeavingEditor() = runTest {
        val repository = TestRepository(UserProfile("Alex", "file:///original.img"))
        val avatars = TestAvatarRepository()
        val vm = createViewModel(repository, avatars = avatars)
        runCurrent()
        vm.selectPhoto("content://gallery/first")
        runCurrent()
        val first = vm.uiState.value.draft.avatarUri
        vm.selectPhoto("content://gallery/second")
        runCurrent()
        assertTrue(avatars.discarded.contains(first))
        vm.save()
        runCurrent()
        val savedUri = repository.current.avatarUri
        assertTrue(avatars.discarded.contains("file:///original.img"))

        store.clear()
        runCurrent()
        assertFalse(avatars.discarded.contains(savedUri))
    }

    @Test
    fun cancelledCameraAndFailedImportKeepPreviousAvatar() = runTest {
        val repository = TestRepository(UserProfile("Alex", "file:///original.img"))
        val avatars = TestAvatarRepository()
        val vm = createViewModel(repository, avatars = avatars)
        runCurrent()
        vm.prepareCamera()
        runCurrent()
        val capture = vm.uiState.value.cameraToLaunch
        vm.cameraLaunched()
        vm.cameraResult(false)
        runCurrent()
        assertTrue(avatars.discarded.contains(capture))

        avatars.importError = IOException("No permission")
        vm.selectPhoto("content://gallery/inaccessible")
        runCurrent()
        assertEquals(EditProfileError.AVATAR, vm.uiState.value.error)
        assertEquals("file:///original.img", vm.uiState.value.draft.avatarUri)
        assertFalse(vm.uiState.value.isImportingAvatar)
    }

    @Test
    fun cameraResultIsRestoredWithoutLaunchingCameraTwice() = runTest {
        val repository = TestRepository(UserProfile("Alex"))
        val avatars = TestAvatarRepository()
        val handle = SavedStateHandle()
        val vm = createViewModel(repository, handle, avatars)
        runCurrent()
        vm.prepareCamera()
        runCurrent()
        vm.cameraLaunched()
        val restoredHandle = SavedStateHandle(handle.keys().associateWith { handle.get<Any?>(it) })
        val restored = EditProfileViewModel(repository, restoredHandle, avatars, cleanupScope, DiscardUnusedAvatar(repository, avatars))
            .also { store.put("restored", it) }
        restored.cameraResult(true)
        runCurrent()

        assertEquals(null, restored.uiState.value.cameraToLaunch)
        assertEquals(1, avatars.cameraRequests)
        assertEquals("file:///private/avatar-1.img", restored.uiState.value.draft.avatarUri)
    }

    private class TestRepository(initial: UserProfile) : ProfileRepository {
        private val values = MutableStateFlow(initial)
        val current get() = values.value
        var readError: IOException? = null
        var writeError: IOException? = null
        var saveGate: CompletableDeferred<Unit>? = null
        var saveAttempts = 0
        override val profile = flow {
            readError?.let { throw it }
            values.collect { emit(it) }
        }

        override suspend fun save(profile: UserProfile) {
            saveAttempts++
            saveGate?.await()
            writeError?.let { throw it }
            values.value = profile
        }
    }
}
