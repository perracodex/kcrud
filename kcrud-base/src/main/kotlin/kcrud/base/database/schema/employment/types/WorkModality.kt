/*
 * Copyright (c) 2024-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.database.schema.employment.types

import kcrud.base.persistence.utils.IEnumWithId

/**
 * The possible employment work modalities.
 */
enum class WorkModality(override val id: Int) : IEnumWithId {
    /** The employee works on-site premises. */
    ON_SITE(id = 100),

    /** The employee works remotely. */
    REMOTE(id = 101),

    /** The employee works in a hybrid modality, so both on-site and remotely. */
    HYBRID(id = 102);

    companion object {
        private val map: Map<Int, WorkModality> = WorkModality.entries.associateBy(WorkModality::id)
        fun fromId(id: Int): WorkModality? = map[id]
    }
}
