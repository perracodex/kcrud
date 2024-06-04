/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.database.schema.employee.types

/**
 * Example of employee marital status. Ideally these should be defined
 * at database level instead of being hardcoded.
 */
enum class MaritalStatus {
    /** Married marital status. */
    MARRIED,

    /** Single marital status. */
    SINGLE,

    /** Divorced marital status. */
    DIVORCED,

    /** Unknown marital status. */
    UNKNOWN
}
