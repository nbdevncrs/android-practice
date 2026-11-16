package com.example.android_practice.profile.presentation.view

import com.example.android_practice.profile.domain.model.ResumeDocument
import com.example.android_practice.profile.domain.model.ResumeError

sealed interface ResumeUiState {
    data object Idle : ResumeUiState
    data object Downloading : ResumeUiState
    data class Ready(val document: ResumeDocument) : ResumeUiState
    data class Error(val reason: ResumeUiError) : ResumeUiState
}

enum class ResumeUiError { INVALID_URL, NETWORK, SERVER, INVALID_FILE, STORAGE, NO_VIEWER }

internal fun ResumeError.toUiError(): ResumeUiError = when (this) {
    ResumeError.INVALID_URL -> ResumeUiError.INVALID_URL
    ResumeError.NETWORK -> ResumeUiError.NETWORK
    ResumeError.SERVER -> ResumeUiError.SERVER
    ResumeError.INVALID_FILE -> ResumeUiError.INVALID_FILE
    ResumeError.STORAGE -> ResumeUiError.STORAGE
}
