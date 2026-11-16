package com.example.android_practice.profile.domain.model

enum class ClassTimeValidation { VALID, REQUIRED, INVALID_FORMAT }

fun validateClassTime(value: String): ClassTimeValidation = when {
    value.isBlank() -> ClassTimeValidation.REQUIRED
    ClassTime.parse(value) == null -> ClassTimeValidation.INVALID_FORMAT
    else -> ClassTimeValidation.VALID
}
