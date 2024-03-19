/*
 * Copyright (c) 2024-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.database.schema.employee.types

/**
 * Example of employee marital status. Ideally these should be defined
 * at database level instead of being hardcoded.
 */
enum class MaritalStatus {
    MARRIED,
    SINGLE,
    DIVORCED,
    UNKNOWN
}
