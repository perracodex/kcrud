/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.scheduler.service.schedule

import kcrud.base.utils.KLocalDateTime
import kotlinx.serialization.Serializable

/**
 * Represents a schedule for a task.
 *
 * Serialization is done using the custom [ScheduleSerializer], which employs polymorphic serialization.
 * Alternatively, could use `@JsonClassDiscriminator` to specify the `type` field discriminator, but
 * this would require the `type` key to always be present in the JSON payload.
 *
 * ```
 * @OptIn(ExperimentalSerializationApi::class)
 * @JsonClassDiscriminator("type")
 * @Serializable
 * sealed class Schedule {
 *    @Serializable
 *    @SerialName("interval")
 *    data class Interval(
 *    ...
 *    }
 *    @Serializable
 *    @SerialName("cron")
 *    data class Cron(
 *    ...
 *    }
 * ```
 *
 * For more on polymorphic serialization, see:
 * - [Serialization with Polymorphism](https://medium.com/livefront/intro-to-polymorphism-with-kotlinx-serialization-b8f5f1cedc99)
 * - [Kotlin Serialization](https://medium.com/lightricks-tech-blog/using-the-kotlin-serialization-library-for-tough-json-serialization-82f8b7ae70dc)
 *
 * @see [ScheduleSerializer]
 */
@Serializable(ScheduleSerializer::class)
sealed class Schedule {
    /** Optional datetime when the task must start. Null to start immediately. */
    abstract val start: KLocalDateTime?

    /**
     * Represents a schedule for a task using a fixed interval.
     *
     * @property start Optional datetime when the task must start. Null to start immediately.
     * @property days The number of days in the interval.
     * @property hours The number of hours in the interval.
     * @property minutes The number of minutes in the interval.
     * @property seconds The number of seconds in the interval.
     */
    @Serializable
    data class Interval(
        override val start: KLocalDateTime? = null,
        val days: UInt = 0u,
        val hours: UInt = 0u,
        val minutes: UInt = 0u,
        val seconds: UInt = 0u
    ) : Schedule() {
        /**
         * Converts the overall interval into a total number of seconds.
         */
        fun toTotalSeconds(): UInt {
            return (days * 24u * 60u * 60u) + (hours * 60u * 60u) + (minutes * 60u) + seconds
        }
    }

    /**
     * Represents a schedule for a task using a cron expression.
     *
     * The cron expression is composed of the following fields:
     * ```
     * ┌───────────── second (0-59)
     * │ ┌───────────── minute (0-59)
     * │ │ ┌───────────── hour (0-23)
     * │ │ │ ┌───────────── day of month (1-31)
     * │ │ │ │ ┌───────────── month (1-12 or JAN-DEC)
     * │ │ │ │ │ ┌───────────── weekday (0-7, SUN-SAT. Both 0 and 7 mean Sunday)
     * │ │ │ │ │ │ ┌───────────── year (optional)
     * │ │ │ │ │ │ │
     * │ │ │ │ │ │ │
     * * * * * * * *
     * ```
     *
     * ```
     * Sample cron expressions:
     *   - "0 0 0 * * ?" - At midnight every day.
     *   - "0 0 12 ? * MON-FRI" - At noon every weekday.
     *   - "0 0/30 9-17 * * ?" - Every 30 minutes between 9 AM to 5 PM.
     *   - "0 0 0 1 * ?" - At midnight on the first day of every month.
     *   - "0 0 6 ? * SUN" - At 6 AM every Sunday.
     *   - "0 0 14 * * ?" - At 2 PM every day.
     *   - "0 15 10 ? * *" - At 10:15 AM every day.
     *   - "0 0/15 * * * ?" - Every 15 minutes.
     *   - "0 0 0 ? * MON#1" - At midnight on the first Monday of every month.
     *   - "30 0 0 * * ?" - At 00:00:30 (30 seconds past midnight) every day.
     *   - "0/1 * * * * ?" - Every second.
     *   - "0 * * * * ?" - Every minute.
     * ```
     *
     * @property start Optional datetime when the task must start. Null to start immediately.
     * @property cron The cron expression at which the task should be executed.
     */
    @Serializable
    data class Cron(
        override val start: KLocalDateTime? = null,
        val cron: String
    ) : Schedule() {
        init {
            require(cron.isNotBlank()) { "Cron expression cannot be blank." }
        }
    }
}
