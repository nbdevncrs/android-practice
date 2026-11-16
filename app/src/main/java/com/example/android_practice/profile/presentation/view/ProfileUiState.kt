package com.example.android_practice.profile.presentation.view

import com.example.android_practice.profile.domain.model.UserProfile

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Content(val profile: UserProfile) : ProfileUiState
    data object Error : ProfileUiState
}
