/*
 * Copyright (c) 2024-Present Perracodex. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.access.rbac.service

import kcrud.access.rbac.entity.base.BaseRbacEntity
import kcrud.base.utils.KLocalDate
import kcrud.base.utils.KLocalDateTime
import kcrud.base.utils.KLocalTime

/**
 * Utility class to anonymize fields based on their type.
 *
 * Enum types are not directly supported for anonymization due to the lack of a standardized approach.
 * A recommended practice is to include an `ANONYMIZED` value within all enum types to facilitate their anonymization.
 *
 * Lists are not supported, as for anonymization the list itself is replaced with an empty list,
 * and we cannot determine the original type of the list's elements. Another approach would
 * have been to anonymize the list's elements, but this would require a recursive approach to
 * anonymize nested lists.
 *
 * Special consideration must be given to the anonymization of time values.
 * The anonymization approach uses "00:00:00.000" as the placeholder for time values.
 * To distinguish between actual instances of midnight and anonymized values, we should consider implementing
 * an additional utility method that would adjust genuine "00:00:00.000" time values, whenever encountered,
 * to a slightly different time (e.g., "00:00:00.001") to prevent confusion with the anonymized time placeholder.
 * Another approach could be to just change entity time fields to text fields, and anonymize them as strings.
 *
 * @see [BaseRbacEntity]
 */
object RbacFieldAnonymization {
    private const val ANONYMIZED_STRING: String = "##########"
    private const val ANONYMIZED_INT: Int = Int.MIN_VALUE
    private const val ANONYMIZED_LONG: Long = Long.MIN_VALUE
    private const val ANONYMIZED_DOUBLE: Double = Double.MIN_VALUE
    private const val ANONYMIZED_FLOAT: Float = Float.MIN_VALUE
    private val ANONYMIZED_DATE: KLocalDate = KLocalDate(year = 1900, monthNumber = 1, dayOfMonth = 1)
    private val ANONYMIZED_TIME: KLocalTime = KLocalTime(hour = 0, minute = 0, second = 0, nanosecond = 0)
    private val ANONYMIZED_DATE_TIME: KLocalDateTime = KLocalDateTime(date = ANONYMIZED_DATE, time = ANONYMIZED_TIME)

    /**
     * Internal method to replace the value of a field with a placeholder based on the field's type.
     *
     * @param value The original value to be anonymized.
     * @return The anonymized value, or the original value if the field type does not match any handled type.
     */
    fun anonymize(value: Any?): Any? {
        return when (value) {
            is String -> ANONYMIZED_STRING
            is Int -> ANONYMIZED_INT
            is Long -> ANONYMIZED_LONG
            is Double -> ANONYMIZED_DOUBLE
            is Float -> ANONYMIZED_FLOAT
            is KLocalDate -> ANONYMIZED_DATE
            is KLocalTime -> ANONYMIZED_TIME
            is KLocalDateTime -> ANONYMIZED_DATE_TIME
            is List<*> -> emptyList<Any?>() // Return an empty list regardless of the element type.
            else -> value
        }
    }

    /**
     * Checks if the given value has been anonymized.
     *
     * @param value The value to check for anonymization.
     * @return True if the value is anonymized, false otherwise.
     */
    fun isAnonymized(value: Any?): Boolean {
        return when (value) {
            is String -> value == ANONYMIZED_STRING
            is Int -> value == ANONYMIZED_INT
            is Long -> value == ANONYMIZED_LONG
            is Double -> value == ANONYMIZED_DOUBLE
            is Float -> value == ANONYMIZED_FLOAT
            is KLocalDate -> value == ANONYMIZED_DATE
            is KLocalTime -> value == ANONYMIZED_TIME
            is KLocalDateTime -> value == ANONYMIZED_DATE_TIME
            else -> false
        }
    }
}
