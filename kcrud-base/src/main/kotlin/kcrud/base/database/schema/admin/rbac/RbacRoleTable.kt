/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.database.schema.admin.rbac

import kcrud.base.database.columns.autoGenerate
import kcrud.base.database.columns.kotlinUuid
import kcrud.base.database.schema.base.TimestampedTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import kotlin.uuid.Uuid

/**
 * Database table definition holding the RBAC Roles.
 *
 * Roles are used to define the access level of an actor to concrete scopes.
 *
 * @see RbacScopeRuleTable
 */
public object RbacRoleTable : TimestampedTable(name = "rbac_role") {
    /**
     * The unique id of the role record.
     */
    public val id: Column<Uuid> = kotlinUuid(
        name = "role_id"
    ).autoGenerate()

    /**
     * The unique name of the role.
     */
    public val role_name: Column<String> = varchar(
        name = "role_name",
        length = 64
    )

    /**
     * Optional description of the role.
     */
    public val description: Column<String?> = varchar(
        name = "description",
        length = 512
    ).nullable()

    /**
     * Whether this is a super-role, in which case it has all permissions granted.
     */
    public val isSuper: Column<Boolean> = bool(
        name = "is_super"
    )

    override val primaryKey: Table.PrimaryKey = PrimaryKey(
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
