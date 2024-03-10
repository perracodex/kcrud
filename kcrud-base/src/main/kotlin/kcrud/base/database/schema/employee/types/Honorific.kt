/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.database.schema.employee.types

import kcrud.base.persistence.utils.IEnumWithId

/**
 * Example in which each item has an id,
 * which is the actual value that will be stored in the database,
 * instead of the name of the enum item.
 */
enum class Honorific(override val id: Int) : IEnumWithId {
    MR(id = 100),
    MRS(id = 101),
    MS(id = 102),
    DR(id = 103),
    MISS(id = 104),
    UNKNOWN(id = 105);

    companion object {
        private val map: Map<Int, Honorific> = Honorific.entries.associateBy(Honorific::id)
        fun fromId(id: Int): Honorific? = map[id]
    }
}
