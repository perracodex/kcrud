/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package krud.base.error

/**
 * Custom exception class to represent unauthorized access errors.
 *
 * @param message The detail message describing the unauthorized access error.
 */
public class UnauthorizedException(message: String) : Exception(message)
