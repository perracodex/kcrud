/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.database.schema.admin.rbac

import kcrud.base.database.schema.admin.rbac.types.RbacAccessLevel
import kcrud.base.database.schema.admin.rbac.types.RbacScope
import kcrud.base.database.schema.base.TimestampedTable
import kcrud.base.persistence.utils.enumById
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import java.util.*

/**
 * Database table definition holding RBAC rules for a concrete [RbacRoleTable] record.
 *
 * A scope can be any concept: a database table, a REST endpoint, a UI element, etc.
 * Is up to the designer to define what a scope is, and act accordingly when its
 * associated RBAC rule is verified.
 *
 * @see RbacRoleTable
 */
object RbacScopeRuleTable : TimestampedTable(name = "rbac_scope_rule") {
    /**
     * The unique id of the scope rule record.
     */
    val id: Column<UUID> = uuid(
        name = "scope_rule_id"
    ).autoGenerate()

    /**
     * The associated [RbacRoleTable] id.
     */
    val roleId: Column<UUID> = uuid(
        name = "role_id"
    ).references(
        fkName = "fk_rbac_scope_rule__role_id",
        ref = RbacRoleTable.id,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.RESTRICT
    )

    /**
     * The [RbacScope] the rule is meant to target.
     */
    val scope: Column<RbacScope> = enumById(
        name = "scope",
        fromId = RbacScope::fromId
    )

    /**
     * The [RbacAccessLevel] representing the access level for the [RbacScope].
     */
    val accessLevel: Column<RbacAccessLevel> = enumById(
        name = "access_level",
        fromId = RbacAccessLevel::fromId
    )

    override val primaryKey: Table.PrimaryKey = PrimaryKey(
        firstColumn = id,
        name = "pk_scope_rule_id"
    )

    init {
        uniqueIndex(
            customIndexName = "uq_rbac_scope_rule__role_id__scope",
            columns = arrayOf(roleId, scope)
        )
    }
}
