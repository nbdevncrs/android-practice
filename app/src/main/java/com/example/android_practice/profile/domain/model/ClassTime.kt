package com.example.android_practice.profile.domain.model

@ConsistentCopyVisibility
data class ClassTime private constructor(val hour: Int, val minute: Int) {
    init {
        require(hour in 0..23 && minute in 0..59)
    }

    fun formatted(): String = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"

    companion object {
        private val format = Regex("([01][0-9]|2[0-3]):[0-5][0-9]")

        fun parse(value: String): ClassTime? {
            if (!format.matches(value)) return null
            return ClassTime(value.substring(0, 2).toInt(), value.substring(3, 5).toInt())
        }
    }
}
