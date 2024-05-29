/*
 * Copyright (c) 2024-Present Perracodex. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.database.schema.employment.types

import kcrud.base.persistence.utils.IEnumWithId

/**
 * The possible employment statuses.
 *
 * @property id The id of the employment status.
 */
enum class EmploymentStatus(override val id: Int) : IEnumWithId {
    /** The employee employment is in the onboarding process. */
    ONBOARDING(id = 100),

    /** The employee employment is currently active. */
    ACTIVE(id = 101),

    /** The employee employment is in an idle state, such as a sabbatical. */
    IDLE(id = 102),

    /** The employee employment has been terminated. */
    TERMINATED(id = 103);

    companion object {
        private val map: Map<Int, EmploymentStatus> = EmploymentStatus.entries.associateBy(EmploymentStatus::id)

        /**
         * Get the [EmploymentStatus] from the given [id].
         *
         * @param id The id of the [EmploymentStatus].
         * @return The [EmploymentStatus] with the given [id].
         */
        fun fromId(id: Int): EmploymentStatus? = map[id]
    }
}
