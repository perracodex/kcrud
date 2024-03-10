/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.database.schema.employment.types

import kcrud.base.persistence.utils.IEnumWithId

/**
 * The possible employment work modalities.
 */
enum class WorkModality(override val id: Int) : IEnumWithId {
    ON_SITE(100),
    REMOTE(101),
    HYBRID(102);

    companion object {
        private val map: Map<Int, WorkModality> = WorkModality.entries.associateBy(WorkModality::id)
        fun fromId(id: Int): WorkModality? = map[id]
    }
}
