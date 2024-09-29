/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.settings.parser

/**
 * Custom exception class to represent errors when parsing configuration files.
 *
 * @param message The detail message describing the failure.
 * @param cause Optional underlying reason for this [ConfigurationException].
 */
public class ConfigurationException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
