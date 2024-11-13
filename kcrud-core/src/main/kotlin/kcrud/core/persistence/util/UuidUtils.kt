/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.persistence.util

import kcrud.core.env.Tracer
import kotlin.uuid.Uuid

/**
 * Converts a [String] to a [Uuid] or returns null if the string is not a valid [Uuid].
 *
 * @return The [Uuid] representation of the string, or null if the string is null or is not a valid [Uuid].
 */
public fun String?.toUuidOrNull(): Uuid? {
    if (this.isNullOrBlank()) return null
    return runCatching {
        Uuid.parse(uuidString = this)
    }.onFailure { e ->
        Tracer(ref = ::toUuidOrNull).error(message = "Failed to parse Uuid from string: '$this'", cause = e)
    }.getOrNull()
}

/**
 * Converts a given string to a [Uuid] object.
 *
 * @return a [Uuid] object converted from the string representation.
 * @throws IllegalArgumentException if the string is not a valid [Uuid].
 */
public fun String?.toUuid(): Uuid {
    requireNotNull(this) { "Uuid string cannot be null." }
    return try {
        Uuid.parse(uuidString = this)
    } catch (e: IllegalArgumentException) {
        throw IllegalArgumentException("String '$this' is not a valid Uuid.", e)
    }
}
