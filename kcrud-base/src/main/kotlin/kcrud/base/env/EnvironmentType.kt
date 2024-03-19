/*
 * Copyright (c) 2023-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
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
