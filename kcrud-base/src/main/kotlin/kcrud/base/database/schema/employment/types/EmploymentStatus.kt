/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.database.schema.employment.types

import kcrud.base.persistence.utils.IEnumWithId

/**
 * The possible employment statuses.
 */
enum class EmploymentStatus(override val id: Int) : IEnumWithId {
    ONBOARDING(100),
    ACTIVE(101),
    TERMINATED(102);

    companion object {
        private val map: Map<Int, EmploymentStatus> = EmploymentStatus.entries.associateBy(EmploymentStatus::id)
        fun fromId(id: Int): EmploymentStatus? = map[id]
    }
}
