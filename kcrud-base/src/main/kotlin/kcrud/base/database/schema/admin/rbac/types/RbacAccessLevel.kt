/*
 * Copyright (c) 2024-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.database.schema.admin.rbac.types

import kcrud.base.persistence.utils.IEnumWithId

/**
 * Enum representing the access level to a resource.
 *
 * This enum can be expanded to encompass more granular permissions, like:
 * DELETE, UPDATE, CREATE, PRINT, etc.
 *
 * In conjunction with [RbacResource], it defines the access level to a resource,
 * facilitating role-based access control.
 *
 * @see RbacResource
 */
enum class RbacAccessLevel(override val id: Int) : IEnumWithId {
    /** No access rights to a resource. */
    NONE(id = 0),

    /** Read-only access to a resource. */
    VIEW(id = 1),

    /** Full unrestricted access rights to a resource. */
    FULL(id = 100);

    /**
     * Determines if the current RBAC access level has sufficient privileges as compared to a required
     * access level, to determine if the current access level encompasses the required permissions.
     *
     * This could also be compared by the id access level, so the higher the id, the higher the privileges,
     * but such approach would mean that the ids would have to be sequential, and it would make it
     * not trivial to create isolated concrete levels of access.
     *
     * @param requiredAccessLevel The [RbacAccessLevel] to be compared against the current access level.
     * @return True if the current access level has sufficient privileges for the required one, False otherwise.
     */
    fun hasSufficientPrivileges(requiredAccessLevel: RbacAccessLevel): Boolean {
        return when (this) {
            FULL -> true // Full level encompasses all privileges.
            VIEW -> (requiredAccessLevel == VIEW)
            NONE -> false
        }
    }

    companion object {
        private val map: Map<Int, RbacAccessLevel> = RbacAccessLevel.entries.associateBy(RbacAccessLevel::id)
        fun fromId(id: Int): RbacAccessLevel? = map[id]
    }
}
