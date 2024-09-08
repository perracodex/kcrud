/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.database.schema.employee.types

import kcrud.base.persistence.utils.IEnumWithId

/**
 * Example in which each item has an id,
 * which is the actual value that will be stored in the database,
 * instead of the name of the enum item.
 *
 * @property id The unique identifier of the honorific.
 */
public enum class Honorific(override val id: Int) : IEnumWithId {
    /** Honorific for Mister. */
    MR(id = 100),

    /** Honorific for Missus. */
    MRS(id = 101),

    /** Honorific for Miss. */
    MS(id = 102),

    /** Honorific for Doctor. */
    DR(id = 103),

    /** Honorific for Miss. */
    MISS(id = 104),

    /** Unknown honorific. */
    UNKNOWN(id = 105);
}
