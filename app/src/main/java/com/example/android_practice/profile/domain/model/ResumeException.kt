package com.example.android_practice.profile.domain.model

class ResumeException(val error: ResumeError, cause: Throwable? = null) : Exception(error.name, cause)
