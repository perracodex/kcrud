/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.database.schema.admin.rbac

import kcrud.base.database.schema.admin.rbac.types.RbacAccessLevel
import kcrud.base.database.schema.base.TimestampedTable
import kcrud.base.persistence.utils.autoGenerate
import kcrud.base.persistence.utils.enumerationById
import kcrud.base.persistence.utils.kotlinUuid
import kcrud.base.persistence.utils.references
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import kotlin.uuid.Uuid

/**
 * Database table definition holding the RBAC field level rules.
 *
 * By default, all database fields should be returned as is,
 * unless added to this table and associated to a [RbacScopeRuleTable] record,
 * in which case the fields should be handled according to their access level.
 */
public object RbacFieldRuleTable : TimestampedTable(name = "rbac_field_rule") {
    /**
     * The unique id of the field rule record.
     */
    public val id: Column<Uuid> = kotlinUuid(
        name = "field_rule_id"
    ).autoGenerate()

    /**
     * The associated [RbacScopeRuleTable] id.
     */
    public val scopeRuleId: Column<Uuid> = kotlinUuid(
        name = "scope_rule_id"
    ).references(
        ref = RbacScopeRuleTable.id,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.RESTRICT,
        fkName = "fk_rbac_field_rule__scope_rule_id"
    )

    /**
     * The name of the field being targeted.
     */
    public val fieldName: Column<String> = varchar(
        name = "field_name",
        length = 64
    )

    /**
     * The [RbacAccessLevel] representing the access level for the field.
     */
    public val accessLevel: Column<RbacAccessLevel> = enumerationById(
        name = "access_level_id",
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
