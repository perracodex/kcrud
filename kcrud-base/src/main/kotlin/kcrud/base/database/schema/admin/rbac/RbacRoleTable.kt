/*
 * Copyright (c) 2024-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.database.schema.admin.rbac

import kcrud.base.database.schema.base.TimestampedTable

/**
 * Database table definition holding the RBAC Roles.
 *
 * Roles are used to define the access level of an actor to concrete resources.
 *
 * @see RbacResourceRuleTable
 */
object RbacRoleTable : TimestampedTable(name = "rbac_role") {
    /**
     * The unique id of the role record.
     */
    val id = uuid(
        name = "role_id"
    ).autoGenerate()

    /**
     * The unique name of the role.
     */
    val role_name = varchar(
        name = "role_name",
        length = 64
    )

    /**
     * Optional description of the role.
     */
    val description = varchar(
        name = "description",
        length = 512
    ).nullable()

    /**
     * Whether this is a super-role, in which case it has all permissions granted.
     */
    val isSuper = bool(
        name = "is_super"
    )

    override val primaryKey = PrimaryKey(
        firstColumn = id,
        name = "pk_role_id"
    )

    init {
        uniqueIndex(
            customIndexName = "uq_rbac_role__role_name",
            columns = arrayOf(role_name)
        )
    }
}
