/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

@file:Suppress("unused")

package kcrud.base.infrastructure.utils

import kcrud.base.admin.rbac.service.RbacFieldAnonymization
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.reflect.KProperty
import kotlin.time.Duration
import kotlin.time.DurationUnit

/**
 * Alias for kotlinx [LocalDate], to avoid ambiguity with Java's LocalDate
 */
typealias KLocalDate = LocalDate

/**
 * Alias for kotlinx [LocalTime], to avoid ambiguity with Java's LocalTime
 */
typealias KLocalTime = LocalTime

/**
 * Alias for kotlinx [LocalDateTime], to avoid ambiguity with Java's LocalDateTime
 */
typealias KLocalDateTime = LocalDateTime

/**
 * Delegate for calculating an Age based on a given date of birth (dob).
 */
class AgeDelegate(private val dob: KLocalDate) {
    operator fun getValue(ignoreThisRef: Any?, ignoreProperty: KProperty<*>): Int {
        return DateTimeUtils.calculateAge(dob = dob)
    }
}

/**
 * Singleton providing time-related utility functions.
 */
object DateTimeUtils {

    enum class DateFormat(val pattern: String) {
        YYYY_MMM_DD("yyyy-MMM-dd")
    }

    fun calculateAge(dob: KLocalDate): Int {

        if (RbacFieldAnonymization.isAnonymized(value = dob)) {
            return RbacFieldAnonymization.anonymize(value = 0) as Int
        }

        // Get today's date based on the system clock and timezone.
        val currentDate: KLocalDate = currentUTCDate()

        // Calculate the difference in years.
        val age: Int = currentDate.year - dob.year

        val birthdayAlreadyPassed: Boolean = (dob.monthNumber < currentDate.monthNumber) ||
                (dob.monthNumber == currentDate.monthNumber && dob.dayOfMonth <= currentDate.dayOfMonth)

        // Adjust the age if the birthday hasn't occurred this year yet.
        return age.takeIf { birthdayAlreadyPassed } ?: (age - 1)
    }

    /**
     * Returns the current date-time in UTC.
     */
    fun currentUTCDateTime(): KLocalDateTime = Clock.System.now().toLocalDateTime(timeZone = TimeZone.UTC)

    /**
     * Returns the current date in UTC.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun currentUTCDate(): KLocalDate = Clock.System.todayIn(timeZone = TimeZone.currentSystemDefault())

    /**
     * Converts a UTC time to the local time zone.
     */
    fun utcToLocal(utc: KLocalDateTime): KLocalDateTime {
        return utc.toInstant(timeZone = TimeZone.UTC).toLocalDateTime(timeZone = TimeZone.currentSystemDefault())
    }

    /**
     * Formats a date using the given [DateFormat] pattern.
     */
    fun format(date: KLocalDate, pattern: DateFormat): String {
        return DateTimeFormatter.ofPattern(pattern.pattern).format(date.toJavaLocalDate())
    }

    /**
     * Converts a Kotlin [LocalDateTime] to a Java [Date].
     */
    fun KLocalDateTime.toJavaDate(): Date {
        this.toInstant(timeZone = TimeZone.UTC).toJavaInstant().let {
            return Date.from(it)
        }
    }

    /**
     * Converts a Kotlin [Duration] to a Java [Instant].
     */
    fun Duration.toInstant(): Instant {
        return Instant.ofEpochMilli(
            System.currentTimeMillis() + this.toLong(unit = DurationUnit.MILLISECONDS)
        )
    }
}
