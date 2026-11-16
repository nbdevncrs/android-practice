package com.example.android_practice.profile.presentation.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.android_practice.profile.domain.model.UserProfile
import com.example.android_practice.profile.domain.model.validate
import com.example.android_practice.profile.domain.repository.ProfileRepository
import com.example.android_practice.profile.domain.repository.AvatarRepository
import com.example.android_practice.profile.domain.usecase.DiscardUnusedAvatar
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val repository: ProfileRepository,
    savedStateHandle: SavedStateHandle,
    private val avatars: AvatarRepository,
    private val cleanupScope: CoroutineScope,
    private val discardUnusedAvatar: DiscardUnusedAvatar,
) : ViewModel() {
    private val editorState = EditProfileSavedState(savedStateHandle)
    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState = _uiState.asStateFlow()
    private var loadJob: Job? = null

    init {
        retryLoading()
    }

    fun retryLoading() {
        if (_uiState.value.isSaving || _uiState.value.isSaved) return
        loadJob?.cancel()
        _uiState.update { it.copy(isLoading = true, error = null) }
        loadJob = viewModelScope.launch {
            try {
                val profile = editorState.draft ?: repository.profile.first()
                currentCoroutineContext().ensureActive()
                updateDraft(profile)
                _uiState.update { it.copy(isLoading = false, cameraToLaunch = editorState.cameraToLaunch) }
            } catch (error: CancellationException) {
                throw error
            } catch (_: IOException) {
                _uiState.update { it.copy(isLoading = false, error = EditProfileError.LOAD) }
            }
        }
    }

    fun setFullName(value: String) {
        if (!_uiState.value.canEdit) return
        updateDraft(_uiState.value.draft.copy(fullName = value))
        _uiState.update { it.copy(validation = it.validation.copy(fullNameRequired = false)) }
    }

    fun setResumeUrl(value: String) {
        if (!_uiState.value.canEdit) return
        updateDraft(_uiState.value.draft.copy(resumeUrl = value))
        _uiState.update { it.copy(validation = it.validation.copy(invalidResumeUrl = false)) }
    }

    fun save() {
        val state = _uiState.value
        if (!state.canEdit) return
        val profile = state.draft.normalized()
        val validation = profile.validate()
        _uiState.update { it.copy(validation = validation, error = null) }
        if (!validation.isValid) return
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                val previousAvatar = repository.profile.first().avatarUri
                repository.save(profile)
                _uiState.update { it.copy(isSaving = false, isSaved = true) }
                if (previousAvatar != profile.avatarUri) discardLater(previousAvatar)
            } catch (error: CancellationException) {
                throw error
            } catch (_: IOException) {
                _uiState.update { it.copy(isSaving = false, error = EditProfileError.SAVE) }
            }
        }
    }

    fun selectPhoto(uri: String?) {
        if (uri != null) importAvatar(uri)
    }

    fun prepareCamera() {
        if (!_uiState.value.canEdit) return
        _uiState.update { it.copy(isImportingAvatar = true, error = null) }
        viewModelScope.launch {
            try {
                val uri = avatars.createCameraUri()
                editorState.prepareCamera(uri)
                _uiState.update { it.copy(isImportingAvatar = false, cameraToLaunch = uri) }
            } catch (error: CancellationException) {
                throw error
            } catch (_: IOException) {
                _uiState.update { it.copy(isImportingAvatar = false, error = EditProfileError.CAMERA) }
            }
        }
    }

    fun cameraLaunched() {
        editorState.cameraLaunched()
        _uiState.update { it.copy(cameraToLaunch = null) }
    }

    fun cameraResult(success: Boolean) {
        val uri = editorState.pendingCameraUri ?: return
        clearCameraRequest()
        if (success) importAvatar(uri, cameraUri = uri) else discardLater(uri)
    }

    fun photoSelectionFailed() {
        _uiState.update { it.copy(error = EditProfileError.AVATAR) }
    }

    fun cameraUnavailable() {
        val uri = editorState.pendingCameraUri
        clearCameraRequest()
        discardLater(uri)
        _uiState.update { it.copy(error = EditProfileError.CAMERA) }
    }

    private fun clearCameraRequest() {
        editorState.clearCamera()
        _uiState.update { it.copy(cameraToLaunch = null) }
    }

    private fun importAvatar(source: String, cameraUri: String? = null) {
        viewModelScope.launch {
            loadJob?.join()
            if (!_uiState.value.canEdit) {
                discardLater(cameraUri)
                return@launch
            }
            _uiState.update { it.copy(isImportingAvatar = true, error = null) }
            var imported: String? = null
            var attached = false
            try {
                val previous = _uiState.value.draft.avatarUri
                imported = avatars.importPhoto(source)
                currentCoroutineContext().ensureActive()
                updateDraft(_uiState.value.draft.copy(avatarUri = imported))
                attached = true
                if (previous != imported) discardLater(previous)
            } catch (error: CancellationException) {
                throw error
            } catch (_: IOException) {
                _uiState.update { it.copy(error = EditProfileError.AVATAR) }
            } finally {
                if (!attached) discardLater(imported)
                _uiState.update { it.copy(isImportingAvatar = false) }
                discardLater(cameraUri)
            }
        }
    }

    private fun discardLater(uri: String?) {
        if (uri == null) return
        cleanupScope.launch {
            try {
                discardUnusedAvatar(uri)
            } catch (_: IOException) {
            }
        }
    }

    override fun onCleared() {
        discardLater(_uiState.value.draft.avatarUri)
        discardLater(editorState.pendingCameraUri)
        super.onCleared()
    }

    private fun updateDraft(profile: UserProfile) {
        editorState.saveDraft(profile)
        _uiState.update { it.copy(draft = profile, error = null) }
    }

    companion object {
        fun factory(
            repository: ProfileRepository,
            avatars: AvatarRepository,
            cleanupScope: CoroutineScope,
            discardUnusedAvatar: DiscardUnusedAvatar,
        ) = viewModelFactory {
            initializer { EditProfileViewModel(repository, createSavedStateHandle(), avatars, cleanupScope, discardUnusedAvatar) }
        }
    }
}
