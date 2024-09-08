/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.database.schema.employee.types

import kcrud.base.persistence.utils.IEnumWithId

/**
 * List of marital statuses that can be used for an employee.
 *
 * @property id The unique identifier for the marital status.
 */
public enum class MaritalStatus(override val id: Int) : IEnumWithId {
    /** Unknown marital status. */
    UNKNOWN(id = 100),

    /** Married marital status. */
    MARRIED(id = 101),

    /** Single marital status. */
    SINGLE(id = 102),

    /** Divorced marital status. */
    DIVORCED(id = 103),

}
