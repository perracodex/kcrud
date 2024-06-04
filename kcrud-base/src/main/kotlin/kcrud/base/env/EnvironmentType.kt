/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.env

/**
 * Represents different types of deployment environments.
 */
enum class EnvironmentType {
    /** Development: for software development and debugging. */
    DEV,

    /** Production: for live or real-world operation. */
    PROD,

    /** Staging: for pre-production testing. */
    STAGING,

    /** Test: for testing, typically for unit testing, mock data, etc. */
    TEST
}
