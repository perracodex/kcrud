/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.persistence.utils

import kotlin.uuid.Uuid

/**
 * Converts a [String] to a [Uuid] or returns null if the string is not a valid [Uuid].
 *
 * @return The [Uuid] representation of the string, or null if the string is null or is not a valid [Uuid].
 */
public fun String?.toUuidOrNull(): Uuid? {
    if (this.isNullOrBlank()) return null
    return try {
        Uuid.parse(uuidString = this)
    } catch (e: IllegalArgumentException) {
        null
    }
}

/**
 * Converts a given string to a [Uuid] object.
 *
 * @return a [Uuid] object converted from the string representation.
 * @throws IllegalArgumentException if the string is not a valid [Uuid].
 */
public fun String?.toUuid(): Uuid {
    return try {
        Uuid.parse(uuidString = this!!)
    } catch (e: IllegalArgumentException) {
        throw IllegalArgumentException("String '$this' is not a valid Uuid.")
    }
}

