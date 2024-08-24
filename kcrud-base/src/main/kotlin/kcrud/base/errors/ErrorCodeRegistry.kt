/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
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
public object ErrorCodeRegistry {
    private val registeredTags: MutableSet<String> = mutableSetOf()

    /**
     * Registers a new error code tag and checks for uniqueness.
     *
     * @param tag The error code tag to be registered, representing the base part of error codes.
     * @throws IllegalArgumentException if the code tag is already registered, indicating a conflict
     *         in error code categorization.
     */
    public fun registerTag(tag: String) {
        require(value = tag !in registeredTags) { "Duplicate error code tag detected: $tag" }
        registeredTags += tag
    }
}

