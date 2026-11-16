package com.example.android_practice.profile.presentation.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.android_practice.profile.domain.repository.ProfileRepository
import com.example.android_practice.profile.domain.repository.ResumeRepository
import com.example.android_practice.profile.domain.model.ResumeDocument
import com.example.android_practice.profile.domain.model.ResumeException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class ProfileViewModel(
    private val repository: ProfileRepository,
    private val resumes: ResumeRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState = _uiState.asStateFlow()
    private var observeJob: Job? = null
    private var observedResumeUrl: String? = null
    private var downloadJob: Job? = null
    private val _resumeState = MutableStateFlow<ResumeUiState>(ResumeUiState.Idle)
    val resumeState = _resumeState.asStateFlow()

    init {
        retry()
    }

    fun retry() {
        observeJob?.cancel()
        _uiState.value = ProfileUiState.Loading
        observeJob = viewModelScope.launch {
            try {
                repository.profile.collect { profile ->
                    currentCoroutineContext().ensureActive()
                    if (observedResumeUrl != null && observedResumeUrl != profile.resumeUrl) cancelResume()
                    observedResumeUrl = profile.resumeUrl
                    _uiState.value = ProfileUiState.Content(profile)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: IOException) {
                currentCoroutineContext().ensureActive()
                _uiState.value = ProfileUiState.Error
            }
        }
    }

    fun openResume() {
        if (_resumeState.value is ResumeUiState.Downloading) return
        val profile = (_uiState.value as? ProfileUiState.Content)?.profile ?: return
        if (profile.resumeUrl.isBlank()) return
        _resumeState.value = ResumeUiState.Downloading
        downloadJob = viewModelScope.launch {
            try {
                val document = resumes.download(profile.resumeUrl)
                currentCoroutineContext().ensureActive()
                _resumeState.value = ResumeUiState.Ready(document)
            } catch (error: CancellationException) {
                throw error
            } catch (error: ResumeException) {
                currentCoroutineContext().ensureActive()
                _resumeState.value = ResumeUiState.Error(error.error.toUiError())
            }
        }
    }

    fun cancelResume() {
        downloadJob?.cancel()
        _resumeState.value = ResumeUiState.Idle
    }

    fun takeResumeToOpen(): ResumeDocument? {
        val ready = _resumeState.value as? ResumeUiState.Ready ?: return null
        return if (_resumeState.compareAndSet(ready, ResumeUiState.Idle)) ready.document else null
    }

    fun resumeOpeningFailed(reason: ResumeUiError) {
        _resumeState.value = ResumeUiState.Error(reason)
    }

    companion object {
        fun factory(repository: ProfileRepository, resumes: ResumeRepository) = viewModelFactory {
            initializer { ProfileViewModel(repository, resumes) }
        }
    }
}
