/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.errors

/**
 * Registry for error code tags, to ensure that the segment (tag) prefix of each error code
 * is unique across the application.
 * This prevents the reuse of tags across different error types, which helps in maintaining
 * distinct and identifiable error codes.
 * It is designed to manage only the static part of the error codes (the tags),
 * as the code suffixes would produce a duplicate every time an error is instantiated
 * in order to be thrown.
 */
object ErrorCodeRegistry {
    private val registeredTags: MutableSet<String> = mutableSetOf()

    /**
     * Registers a new error code tag and checks for uniqueness.
     *
     * @param tag The error code tag to be registered, representing the base part of error codes.
     * @throws IllegalArgumentException if the code tag is already registered, indicating a conflict
     *         in error code categorization.
     */
    fun registerTag(tag: String) {
        require(value = tag !in registeredTags) { "Duplicate error code tag detected: $tag" }
        registeredTags += tag
    }
}

