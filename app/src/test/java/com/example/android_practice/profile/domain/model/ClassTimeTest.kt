package com.example.android_practice.profile.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ClassTimeTest {
    @Test
    fun everyMinuteOfTheDayCanBeParsedAndFormatted() {
        for (hour in 0..23) {
            for (minute in 0..59) {
                val value = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
                val time = requireNotNull(ClassTime.parse(value))
                assertEquals(hour, time.hour)
                assertEquals(minute, time.minute)
                assertEquals(value, time.formatted())
                assertEquals(ClassTimeValidation.VALID, validateClassTime(value))
            }
        }
    }

    @Test
    fun emptyOrBlankTimeIsRequired() {
        listOf("", " ", "\t").forEach { value ->
            assertNull(ClassTime.parse(value))
            assertEquals(ClassTimeValidation.REQUIRED, validateClassTime(value))
        }
    }

    @Test
    fun invalidRangesAndNonStrictFormatsAreRejected() {
        listOf("24:00", "23:60", "-1:00", "9:05", "09:5", "09.05", "09:05:00",
            " 09:05", "09:05 ", "09:05\n", "ab:cd", "０９:０５").forEach { value ->
            assertNull(value, ClassTime.parse(value))
            assertEquals(value, ClassTimeValidation.INVALID_FORMAT, validateClassTime(value))
        }
    }
}
