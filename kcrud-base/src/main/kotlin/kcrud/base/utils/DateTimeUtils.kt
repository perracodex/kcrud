/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.utils

import kcrud.base.persistence.serializers.OffsetTimestamp
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.time.Duration
import kotlin.time.DurationUnit
import java.time.Instant as JavaInstant

/** Alias for kotlinx [LocalDate], to avoid ambiguity with Java's LocalDate. */
public typealias KLocalDate = LocalDate

/** Alias for kotlinx [LocalTime], to avoid ambiguity with Java's LocalTime. */
public typealias KLocalTime = LocalTime

/** Alias for kotlinx [LocalDateTime], to avoid ambiguity with Java's LocalDateTime. */
public typealias KLocalDateTime = LocalDateTime

/** Alias for kotlinx [Instant], to avoid ambiguity with Java's Instant. */
public typealias KInstant = Instant

/**
 * Singleton providing time-related utility functions.
 */
public object DateTimeUtils {

    /**
     * Enum class for date formats.
     *
     * @property pattern The pattern to format the date.
     */
    public enum class DateFormat(public val pattern: String) {
        /** Format: yyyy-MM-dd, e.g. 2024-04-01 */
        YYYY_MM_DD("yyyy-MM-dd"),

        /** Format: yyyy-MMM-dd, e.g. 2024-APR-01 */
        YYYY_MMM_DD("yyyy-MMM-dd")
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
     */
    public fun timezone(): TimeZone {
        return TimeZone.currentSystemDefault()
    }

    /**
     * Calculates the age based on the date of birth.
     *
     * @param dob The date of birth to calculate the age from.
     */
    public fun age(dob: KLocalDate): Int {

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
    public fun currentUTCDateTime(): KLocalDateTime = Clock.System.now().toLocalDateTime(timeZone = timezone())

    /**
     * Returns the current date-time with the specified or default time zone.
     *
     * @param zoneId The time zone ID, defaulting to the system's default time zone.
     * @return An [OffsetTimestamp] representing the current moment in the specified time zone.
     */
    public fun currentZonedTimestamp(zoneId: ZoneId = ZoneId.systemDefault()): OffsetTimestamp {
        return OffsetDateTime.now(zoneId)
    }

    /**
     * Returns the current date in UTC.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    public fun currentUTCDate(): KLocalDate = Clock.System.todayIn(timeZone = timezone())

    /**
     * Converts a UTC time to the local time zone.
     */
    public fun utcToLocal(utc: KLocalDateTime): KLocalDateTime {
        return utc.toInstant(timeZone = TimeZone.UTC).toLocalDateTime(timeZone = timezone())
    }

    /**
     * Formats a date using the given [DateFormat] pattern.
     */
    public fun format(date: KLocalDate, pattern: DateFormat): String {
        return DateTimeFormatter.ofPattern(pattern.pattern).format(date.toJavaLocalDate())
    }

    /**
     * Converts a Kotlin [LocalDateTime] to a Java [Date].
     */
    public fun KLocalDateTime.toJavaDate(): Date {
        this.toInstant(timeZone = timezone()).toJavaInstant().let {
            return Date.from(it)
        }
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
     * Converts a Java [Date] to a Kotlin [LocalDateTime] in the specified time zone.
     *
     * @param datetime The [Date] to be converted.
     * @param zoneId The time zone to apply during the conversion. Defaults to the system's default time zone.
     * @return A [KLocalDateTime] representing the same moment in time as the input [Date], adjusted to the specified time zone.
     */
    public fun javaDateToLocalDateTime(datetime: Date, zoneId: ZoneId = ZoneId.systemDefault()): KLocalDateTime {
        val localDateTime: java.time.LocalDateTime = datetime.toInstant()
            .atZone(zoneId)
            .toLocalDateTime()

        return localDateTime.toKotlinLocalDateTime()
    }

    /**
     * Extension function flattening the time to 00:00:00.000000000.
     */
    public fun KLocalDateTime.flattenTime(): KLocalDateTime {
        return KLocalDateTime(
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
    public fun KLocalDateTime.fillTime(): KLocalDateTime {
        return KLocalDateTime(
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
