/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.persistence.utils

import kcrud.base.persistence.serializers.SUUID
import java.util.*

/**
 * Converts a [String] to a [UUID] or returns null if the string is not a valid UUID.
 *
 * @return The [UUID] representation of the string, or null if the string is null or is not a valid UUID.
 */
fun String?.toUUIDOrNull(): SUUID? {
    if (this.isNullOrBlank()) return null
    return try {
        UUID.fromString(this)
    } catch (e: IllegalArgumentException) {
        null
    }
}

/**
 * Converts a given string representation of a UUID to a UUID object.
 *
 * @return a UUID object converted from the string representation.
 * @throws IllegalArgumentException if the string is not a valid UUID.
 */
fun String?.toUUID(): SUUID {
    return try {
        UUID.fromString(this)
    } catch (e: IllegalArgumentException) {
        throw IllegalArgumentException("String '$this' is not a valid UUID.")
    }
}
