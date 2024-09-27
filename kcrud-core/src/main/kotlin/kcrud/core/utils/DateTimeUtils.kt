/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.utils

import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import kotlinx.serialization.Serializable
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.time.Duration
import kotlin.time.DurationUnit
import java.time.Instant as JavaInstant

/**
 * Singleton providing time-related utility functions.
 */
public object DateTimeUtils {

    /**
     * Enum class for date formats.
     *
     * @property pattern The pattern to format the date.
     */
    public enum class Format(public val pattern: String) {
        /** Format: yyyy-MM-dd, e.g. 2024-04-01 */
        YYYY_MM_DD("yyyy-MM-dd"),

        /** Format: yyyy-MMM-dd, e.g. 2024-APR-01 */
        YYYY_MMM_DD("yyyy-MMM-dd")
    }

    /**
     * Calculates an integer age considering the date.
     */
    public fun LocalDate.age(): Int {

        // Get today's date based on the system clock and timezone.
        val currentDate: LocalDate = LocalDate.current()

        // Calculate the difference in years.
        val age: Int = currentDate.year - this.year

        val birthdayAlreadyPassed: Boolean = (this.monthNumber < currentDate.monthNumber) ||
                (this.monthNumber == currentDate.monthNumber && this.dayOfMonth <= currentDate.dayOfMonth)

        // Adjust the age if the birthday hasn't occurred this year yet.
        return age.takeIf { birthdayAlreadyPassed } ?: (age - 1)
    }

    /**
     * Represents a time interval.
     *
     * @property days The number of days in the interval.
     * @property hours The number of hours in the interval.
     * @property minutes The number of minutes in the interval.
     */
    @Serializable
    public data class Interval(val days: UInt = 0u, val hours: UInt = 0u, val minutes: UInt = 0u) {
        /**
         * Converts the overall interval into a total number of minutes.
         */
        public fun toTotalMinutes(): UInt {
            return (days * 24u * 60u) + (hours * 60u) + minutes
        }
    }

    /**
     * Returns the system's default timezone.
     *
     * Equivalent to [TimeZone.currentSystemDefault].
     */
    public fun TimeZone.Companion.current(): TimeZone {
        return currentSystemDefault()
    }

    /**
     * Returns the current date-time in the system's default time zone.
     */
    public fun LocalDateTime.Companion.current(): LocalDateTime {
        return Clock.System.now().toLocalDateTime(timeZone = TimeZone.current())
    }

    /**
     * Returns the current date in the system's default time zone.
     */
    private fun LocalDate.Companion.current(): LocalDate {
        return Clock.System.todayIn(timeZone = TimeZone.current())
    }

    /**
     * Returns the current date-time in UTC.
     */
    public fun Instant.Companion.current(): Instant = Clock.System.now()

    /**
     * Formats a date using the given [Format] pattern.
     */
    public fun LocalDate.format(pattern: Format): String {
        return DateTimeFormatter.ofPattern(pattern.pattern).format(this.toJavaLocalDate())
    }

    /**
     * Formats a date-time using the given [Format] pattern.
     */
    public fun LocalDateTime.format(pattern: Format): String {
        return DateTimeFormatter.ofPattern(pattern.pattern).format(this.toJavaLocalDateTime())
    }

    /**
     * Converts a Kotlin [LocalDateTime] to a Java [Date].
     */
    public fun LocalDateTime.toJavaDate(): Date {
        this.toInstant(timeZone = TimeZone.current()).toJavaInstant().let { instant ->
            return Date.from(instant)
        }
    }

    /**
     * Converts a Java [Date] to a Kotlin [LocalDateTime].
     *
     * @param zoneId The java time zone to apply during the conversion. Defaults to the system's default time zone.
     */
    public fun Date.toKotlinLocalDateTime(zoneId: ZoneId = ZoneId.systemDefault()): LocalDateTime {
        return this.toInstant()
            .atZone(zoneId)
            .toLocalDateTime()
            .toKotlinLocalDateTime()
    }

    /**
     * Converts a Kotlin [Duration] to a Java [Instant].
     */
    public fun Duration.toJavaInstant(): JavaInstant {
        return JavaInstant.ofEpochMilli(
            System.currentTimeMillis() + this.toLong(unit = DurationUnit.MILLISECONDS)
        )
    }

    /**
     * Extension function flattening the time to 00:00:00.000000000.
     */
    public fun LocalDateTime.flattenTime(): LocalDateTime {
        return LocalDateTime(
            year = year,
            month = month,
            dayOfMonth = dayOfMonth,
            hour = 0,
            minute = 0,
            second = 0,
            nanosecond = 0
        )
    }

    /**
     * Extension function filling the time with 23:59:59.999999999.
     */
    public fun LocalDateTime.fillTime(): LocalDateTime {
        return LocalDateTime(
            year = year,
            month = month,
            dayOfMonth = dayOfMonth,
            hour = 59,
            minute = 59,
            second = 59,
            nanosecond = 999999999
        )
    }
}
