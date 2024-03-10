/*
 * Copyright (c) 2023-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.infrastructure.env

/**
 * The supported deployment types.
 */
enum class EnvironmentType {
    /** Development environment. */
    DEV,

    /** Production environment. */
    PROD,

    /** Staging environment. */
    STAGING,

    /** Testing environment. */
    TEST
}
