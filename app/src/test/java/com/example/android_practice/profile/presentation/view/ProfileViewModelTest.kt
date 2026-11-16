package com.example.android_practice.profile.presentation.view

import androidx.lifecycle.ViewModelStore
import com.example.android_practice.catalog.presentation.MainDispatcherRule
import com.example.android_practice.profile.domain.model.UserProfile
import com.example.android_practice.profile.domain.repository.ProfileRepository
import com.example.android_practice.profile.domain.model.ResumeDocument
import com.example.android_practice.profile.domain.model.ResumeError
import com.example.android_practice.profile.domain.model.ResumeException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()
    private val store = ViewModelStore()

    @After
    fun tearDown() = store.clear()

    @Test
    fun waitsForProfileAndHandlesAnEmptyProfileAsContent() = runTest {
        val repository = TestProfileRepository()
        val vm = createViewModel(repository)
        runCurrent()
        assertEquals(ProfileUiState.Loading, vm.uiState.value)

        repository.save(UserProfile())
        runCurrent()
        assertEquals(ProfileUiState.Content(UserProfile()), vm.uiState.value)
    }

    @Test
    fun updatesWhenSavedProfileChangesAndStopsObservingAfterClearing() = runTest {
        val repository = TestProfileRepository()
        val vm = createViewModel(repository)
        val initial = UserProfile("Alex", null, "https://example.com/resume.pdf")
        repository.save(initial)
        runCurrent()
        assertEquals(ProfileUiState.Content(initial), vm.uiState.value)

        val updated = initial.copy(fullName = "Alex Morgan", avatarUri = "file:///avatar.jpg")
        repository.save(updated)
        runCurrent()
        assertEquals(ProfileUiState.Content(updated), vm.uiState.value)

        store.clear()
        runCurrent()
        assertEquals(0, repository.activeObservers)
        repository.save(UserProfile())
        runCurrent()
        assertEquals(ProfileUiState.Content(updated), vm.uiState.value)
    }

    @Test
    fun failedReadShowsErrorAndRetryLoadsSavedProfile() = runTest {
        val repository = TestProfileRepository().apply { readError = IOException("Storage unavailable") }
        val vm = createViewModel(repository)
        runCurrent()
        assertEquals(ProfileUiState.Error, vm.uiState.value)

        repository.readError = null
        repository.save(UserProfile(fullName = "Alex"))
        vm.retry()
        assertEquals(ProfileUiState.Loading, vm.uiState.value)
        runCurrent()
        assertEquals(ProfileUiState.Content(UserProfile(fullName = "Alex")), vm.uiState.value)
        assertTrue(repository.activeObservers == 1)
    }

    private fun createViewModel(repository: ProfileRepository, resumes: TestResumeRepository = TestResumeRepository()) =
        ProfileViewModel(repository, resumes).also { store.put("profile", it) }

    @Test
    fun downloadBlocksDuplicateRequestsAndDocumentIsConsumedOnlyOnce() = runTest {
        val profiles = TestProfileRepository()
        profiles.save(UserProfile("Alex", resumeUrl = "https://example.com/resume.pdf"))
        val gate = CompletableDeferred<ResumeDocument>()
        val resumes = TestResumeRepository().apply { handler = { gate.await() } }
        val vm = createViewModel(profiles, resumes)
        runCurrent()
        vm.openResume()
        vm.openResume()
        runCurrent()
        assertEquals(ResumeUiState.Downloading, vm.resumeState.value)
        assertEquals(1, resumes.requests.size)
        val document = ResumeDocument("/private/resume.pdf")
        gate.complete(document)
        runCurrent()

        assertEquals(document, vm.takeResumeToOpen())
        assertEquals(null, vm.takeResumeToOpen())
        assertEquals(ResumeUiState.Idle, vm.resumeState.value)
    }

    @Test
    fun downloadFailureCanBeRetriedAndMissingViewerIsReportedWithoutLosingProfile() = runTest {
        val profiles = TestProfileRepository()
        val profile = UserProfile("Alex", resumeUrl = "https://example.com/resume.pdf")
        profiles.save(profile)
        val resumes = TestResumeRepository().apply { handler = { throw ResumeException(ResumeError.NETWORK) } }
        val vm = createViewModel(profiles, resumes)
        runCurrent()
        vm.openResume()
        runCurrent()
        assertEquals(ResumeUiState.Error(ResumeUiError.NETWORK), vm.resumeState.value)
        resumes.handler = { ResumeDocument("/private/resume.pdf") }
        vm.openResume()
        runCurrent()
        assertTrue(vm.resumeState.value is ResumeUiState.Ready)
        vm.takeResumeToOpen()
        vm.resumeOpeningFailed(ResumeUiError.NO_VIEWER)
        assertEquals(ResumeUiState.Error(ResumeUiError.NO_VIEWER), vm.resumeState.value)
        assertEquals(ProfileUiState.Content(profile), vm.uiState.value)
    }

    @Test
    fun changingResumeUrlCancelsOldDownloadAndDoesNotOpenItsResult() = runTest {
        val profiles = TestProfileRepository()
        profiles.save(UserProfile("Alex", resumeUrl = "https://example.com/old.pdf"))
        val gate = CompletableDeferred<ResumeDocument>()
        val resumes = TestResumeRepository().apply { handler = { gate.await() } }
        val vm = createViewModel(profiles, resumes)
        runCurrent()
        vm.openResume()
        runCurrent()
        profiles.save(UserProfile("Alex", resumeUrl = "https://example.com/new.pdf"))
        runCurrent()
        gate.complete(ResumeDocument("/private/old.pdf"))
        runCurrent()

        assertEquals(ResumeUiState.Idle, vm.resumeState.value)
        assertEquals(null, vm.takeResumeToOpen())
    }

    private class TestProfileRepository : ProfileRepository {
        private val values = MutableStateFlow<UserProfile?>(null)
        var readError: IOException? = null
        var activeObservers = 0
        override val profile = flow {
            readError?.let { throw it }
            activeObservers++
            try {
                values.filterNotNull().collect { emit(it) }
            } finally {
                activeObservers--
            }
        }

        override suspend fun save(profile: UserProfile) {
            values.value = profile
        }
    }
}
