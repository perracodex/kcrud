/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.database.schema.employee.type

import kcrud.database.column.IEnumWithId

/**
 * List of honorifics that can be used for an employee.
 *
 * @property id The unique identifier for the honorific.
 */
public enum class Honorific(override val id: Int) : IEnumWithId {
    /** Unknown honorific. */
    UNKNOWN(id = 100),

    /** Honorific for Mister. */
    MR(id = 101),

    /** Honorific for Missus. */
    MRS(id = 102),

    /** Honorific for Miss. */
    MS(id = 103),

    /** Honorific for Doctor. */
    DR(id = 104),

    /** Honorific for Miss. */
    MISS(id = 105)
}
