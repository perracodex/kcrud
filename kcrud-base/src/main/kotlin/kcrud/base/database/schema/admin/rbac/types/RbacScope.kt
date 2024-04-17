/*
 * Copyright (c) 2024-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.database.schema.admin.rbac.types

import kcrud.base.persistence.utils.IEnumWithId

/**
 * Enum to represent the scopes that can be accessed.
 *
 * A scope can be any concept: a database table, a REST endpoint, a UI element, etc.
 * Is up to the designer to define what a scope is, and act accordingly when its
 * associated RBAC rule is verified.
 *
 * In conjunction with [RbacAccessLevel], this enum defines the access level to a scope.
 *
 * @see RbacAccessLevel
 */
enum class RbacScope(override val id: Int) : IEnumWithId {
    RBAC_ADMIN(id = 100),
    SYSTEM(id = 101),
    EMPLOYEE_RECORDS(id = 102),
    EMPLOYEE_CONTACT_RECORDS(id = 103),
    EMPLOYMENT_RECORDS(id = 104);

    companion object {
        private val map: Map<Int, RbacScope> = RbacScope.entries.associateBy(RbacScope::id)
        fun fromId(id: Int): RbacScope? = map[id]
    }
}
