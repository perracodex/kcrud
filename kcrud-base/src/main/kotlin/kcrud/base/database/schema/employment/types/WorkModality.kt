/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.database.schema.employment.types

import kcrud.base.persistence.utils.IEnumWithId

/**
 * The possible employment work modalities.
 *
 * @property id The unique identifier of the work modality.
 */
public enum class WorkModality(override val id: Int) : IEnumWithId {
    /** The employee works on-site premises. */
    ON_SITE(id = 100),

    /** The employee works remotely. */
    REMOTE(id = 101),

    /** The employee works in a hybrid modality, so both on-site and remotely. */
    HYBRID(id = 102);

    internal companion object {
        private val map: Map<Int, WorkModality> = WorkModality.entries.associateBy(WorkModality::id)

        /**
         * Retrieves the work modality from its unique identifier.
         *
         * @param id The unique identifier of the work modality.
         * @return The work modality associated with the given identifier.
         */
        fun fromId(id: Int): WorkModality? = map[id]
    }
}
