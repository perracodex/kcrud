/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.scheduler.service.schedule

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

/**
 * Polymorphic JSON serializer for [Schedule] objects.
 */
internal object ScheduleSerializer : JsonContentPolymorphicSerializer<Schedule>(Schedule::class) {

    override fun selectDeserializer(element: JsonElement): KSerializer<out Schedule> {
        val jsonObject: JsonObject = element.jsonObject
        val lowerCaseKeys: Set<String> = jsonObject.keys.map { it.lowercase() }.toSet()

        // Warn about unsupported combinations of schedule types.
        val hasCron: Boolean = "cron" in lowerCaseKeys
        val hasInterval: Boolean = setOf("days", "hours", "minutes", "seconds").any { it in lowerCaseKeys }
        require(!(hasCron && hasInterval)) {
            "Unsupported schedule. 'cron' cannot be combined with 'days, hours, minutes or 'seconds'."
        }

        // Resolve the schedule type based on the available keys.
        return when {
            "cron" in lowerCaseKeys -> Schedule.Cron.serializer()
            setOf("days", "hours", "minutes").any { it in lowerCaseKeys } -> Schedule.Interval.serializer()
            else -> throw IllegalArgumentException(
                "Unsupported Schedule type. Expected either 'cron' or 'days, " +
                        "hours, minutes or seconds'. Got: ${jsonObject.keys}"
            )
        }
    }
}
