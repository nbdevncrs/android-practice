package com.example.android_practice.profile.domain.usecase

import com.example.android_practice.profile.domain.model.ClassTime
import com.example.android_practice.profile.domain.model.UserProfile
import com.example.android_practice.profile.domain.repository.ProfileRepository
import com.example.android_practice.reminders.domain.model.ClassReminder
import com.example.android_practice.reminders.domain.model.ReminderError
import com.example.android_practice.reminders.domain.model.ReminderException
import com.example.android_practice.reminders.domain.repository.ReminderClock
import com.example.android_practice.reminders.domain.repository.ReminderScheduler
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveProfileAndScheduleReminderTest {
    private val profiles = object : ProfileRepository {
        override val profile = MutableStateFlow(UserProfile())
        var failure: IOException? = null
        override suspend fun save(profile: UserProfile) {
            failure?.let { throw it }
            this.profile.value = profile
        }
    }
    private val scheduler = object : ReminderScheduler {
        var scheduled: ClassReminder? = null
        var failure: ReminderError? = null
        override suspend fun schedule(reminder: ClassReminder) {
            failure?.let { throw ReminderException(it) }
            scheduled = reminder
        }
        override suspend fun cancel() { scheduled = null }
    }
    private val clock = object : ReminderClock {
        var now = 0L
        override fun nowMillis(): Long = now
        override fun todayAt(time: ClassTime): Long = (time.hour * 60L + time.minute) * 60_000L
    }
    private val save = SaveProfileAndScheduleReminder(profiles, scheduler, clock)

    @Test
    fun invalidOrPastTimeDoesNotChangeProfileOrReminder() = runTest {
        listOf("", "9:05", "24:00").forEach { time ->
            assertTrue(save(UserProfile("Alex", favoriteClassTime = time)) is SaveProfileResult.InvalidProfile)
        }
        clock.now = 10 * 60 * 60_000L
        assertEquals(SaveProfileResult.TimePassed, save(UserProfile("Alex", favoriteClassTime = "10:00")))
        assertEquals(SaveProfileResult.TimePassed, save(UserProfile("Alex", favoriteClassTime = "09:00")))
        assertEquals(UserProfile(), profiles.profile.value)
        assertNull(scheduler.scheduled)
    }

    @Test
    fun successfulSaveReplacesReminderAndSnapshotsNormalizedName() = runTest {
        val profile = UserProfile(" Alex ", favoriteClassTime = "09:05")
        assertEquals(SaveProfileResult.Saved, save(profile))
        assertEquals(ClassReminder(32_700_000L, "Alex"), scheduler.scheduled)
        profiles.save(profiles.profile.value.copy(fullName = "Changed separately"))
        assertEquals("Alex", scheduler.scheduled?.ownerName)

        assertEquals(SaveProfileResult.Saved, save(profile.copy(fullName = "Morgan", favoriteClassTime = "10:00")))
        assertEquals(ClassReminder(36_000_000L, "Morgan"), scheduler.scheduled)
    }

    @Test
    fun storageFailureDoesNotScheduleReminder() = runTest {
        profiles.failure = IOException("Storage unavailable")
        assertEquals(SaveProfileResult.StorageFailure, save(UserProfile("Alex", favoriteClassTime = "09:05")))
        assertNull(scheduler.scheduled)
    }

    @Test
    fun schedulingFailureReportsThatProfileHasAlreadyBeenSaved() = runTest {
        val profile = UserProfile("Alex", favoriteClassTime = "09:05")
        scheduler.failure = ReminderError.EXACT_ALARM_NOT_ALLOWED
        assertEquals(SaveProfileResult.ReminderFailure(ReminderError.EXACT_ALARM_NOT_ALLOWED), save(profile))
        assertEquals(profile, profiles.profile.value)
        assertNull(scheduler.scheduled)
    }
}
