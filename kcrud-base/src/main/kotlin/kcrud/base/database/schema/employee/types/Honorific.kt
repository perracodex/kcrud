/*
 * Copyright (c) 2024-Present Perracodex. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
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
enum class Honorific(override val id: Int) : IEnumWithId {
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

    companion object {
        private val map: Map<Int, Honorific> = Honorific.entries.associateBy(Honorific::id)

        /**
         * Retrieves the [Honorific] item corresponding to the given [id].
         *
         * @param id The unique identifier of the honorific.
         * @return The [Honorific] item corresponding to the given [id].
         */
        fun fromId(id: Int): Honorific? = map[id]
    }
}
