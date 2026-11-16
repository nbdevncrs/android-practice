package com.example.android_practice.profile.domain.repository

import com.example.android_practice.profile.domain.model.ResumeDocument

interface ResumeRepository {
    suspend fun download(url: String): ResumeDocument
}
