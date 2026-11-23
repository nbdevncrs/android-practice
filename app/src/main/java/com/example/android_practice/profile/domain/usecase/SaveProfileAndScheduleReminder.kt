package com.example.android_practice.profile.domain.usecase

import com.example.android_practice.profile.domain.model.ClassTime
import com.example.android_practice.profile.domain.model.ClassTimeValidation
import com.example.android_practice.profile.domain.model.UserProfile
import com.example.android_practice.profile.domain.model.validate
import com.example.android_practice.profile.domain.model.validateClassTime
import com.example.android_practice.profile.domain.repository.ProfileRepository
import com.example.android_practice.reminders.domain.model.ClassReminder
import com.example.android_practice.reminders.domain.model.ReminderException
import com.example.android_practice.reminders.domain.repository.ReminderClock
import com.example.android_practice.reminders.domain.repository.ReminderScheduler
import java.io.IOException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SaveProfileAndScheduleReminder(
    private val profiles: ProfileRepository,
    private val scheduler: ReminderScheduler,
    private val clock: ReminderClock,
) {
    private val mutex = Mutex()

    fun validate(draft: UserProfile): SaveProfileResult? {
        val profile = draft.normalized()
        val validation = profile.validate()
        val timeValidation = validateClassTime(profile.favoriteClassTime)
        if (!validation.isValid || timeValidation != ClassTimeValidation.VALID) {
            return SaveProfileResult.InvalidProfile(validation, timeValidation)
        }
        val time = requireNotNull(ClassTime.parse(profile.favoriteClassTime))
        val triggerAt = clock.todayAt(time)
        return if (triggerAt <= clock.nowMillis()) SaveProfileResult.TimePassed else null
    }

    suspend operator fun invoke(draft: UserProfile): SaveProfileResult = mutex.withLock {
        validate(draft)?.let { return@withLock it }
        val profile = draft.normalized()
        val triggerAt = clock.todayAt(requireNotNull(ClassTime.parse(profile.favoriteClassTime)))
        try {
            profiles.save(profile)
        } catch (_: IOException) {
            return@withLock SaveProfileResult.StorageFailure
        }
        try {
            scheduler.schedule(ClassReminder(triggerAt, profile.fullName))
            SaveProfileResult.Saved
        } catch (error: ReminderException) {
            SaveProfileResult.ReminderFailure(error.error)
        }
    }
}
