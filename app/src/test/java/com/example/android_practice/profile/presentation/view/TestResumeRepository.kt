package com.example.android_practice.profile.presentation.view

import com.example.android_practice.profile.domain.model.ResumeDocument
import com.example.android_practice.profile.domain.repository.ResumeRepository

internal class TestResumeRepository : ResumeRepository {
    val requests = mutableListOf<String>()
    var handler: suspend (String) -> ResumeDocument = { ResumeDocument("/private/resume.pdf") }

    override suspend fun download(url: String): ResumeDocument {
        requests += url
        return handler(url)
    }
}
