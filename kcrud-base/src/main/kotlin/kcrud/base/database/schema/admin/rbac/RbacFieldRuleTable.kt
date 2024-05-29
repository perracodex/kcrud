/*
 * Copyright (c) 2024-Present Perracodex. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.database.schema.admin.rbac

import kcrud.base.database.schema.admin.rbac.types.RbacAccessLevel
import kcrud.base.database.schema.base.TimestampedTable
import kcrud.base.persistence.utils.enumById
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import java.util.*

/**
 * Database table definition holding the RBAC field level rules.
 *
 * By default, all database fields should be returned as is,
 * unless added to this table and associated to a [RbacScopeRuleTable] record,
 * in which case the fields should be handled according to their access level.
 */
object RbacFieldRuleTable : TimestampedTable(name = "rbac_field_rule") {
    /**
     * The unique id of the field rule record.
     */
    val id: Column<UUID> = uuid(
        name = "field_rule_id"
    ).autoGenerate()

    /**
     * The associated [RbacScopeRuleTable] id.
     */
    val scopeRuleId: Column<UUID> = uuid(
        name = "scope_rule_id"
    ).references(
        fkName = "fk_rbac_field_rule__scope_rule_id",
        ref = RbacScopeRuleTable.id,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.RESTRICT
    )

    /**
     * The name of the field being targeted.
     */
    val fieldName: Column<String> = varchar(
        name = "field_name",
        length = 64
    )

    /**
     * The [RbacAccessLevel] representing the access level for the field.
     */
    val accessLevel: Column<RbacAccessLevel> = enumById(
        name = "access_level",
        fromId = RbacAccessLevel::fromId
    )

    override val primaryKey: Table.PrimaryKey = PrimaryKey(
        firstColumn = id,
        name = "pk_field_rule_id"
    )

    init {
        uniqueIndex(
            customIndexName = "uq_rbac_field_rule__scope_rule_id__field_name",
            columns = arrayOf(scopeRuleId, fieldName)
        )
    }
}
