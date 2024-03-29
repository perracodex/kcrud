/*
 * Copyright (c) 2024-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.database.schema.admin.rbac

import kcrud.base.database.schema.admin.rbac.types.RbacAccessLevel
import kcrud.base.persistence.utils.enumById
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

/**
 * Database table definition holding the RBAC field level rules.
 *
 * By default, all database fields should be returned as is,
 * unless added to this table and associated to a [RbacResourceRuleTable] record,
 * in which case the fields should be handled according to their access level.
 */
object RbacFieldRuleTable : Table(name = "rbac_field_rule") {
    /**
     * The unique id of the field rule record.
     */
    val id = uuid(
        name = "field_rule_id"
    ).autoGenerate()

    /**
     * The associated [RbacResourceRuleTable] id.
     */
    val resourceRuleId = uuid(
        name = "resource_rule_id"
    ).references(
        fkName = "fk_rbac_field_rule__resource_rule_id",
        ref = RbacResourceRuleTable.id,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.RESTRICT
    )

    /**
     * The name of the field being targeted.
     */
    val fieldName = varchar(
        name = "field_name",
        length = 64
    )

    /**
     * The [RbacAccessLevel] representing the access level for the field.
     */
    val accessLevel = enumById(
        name = "access_level",
        fromId = RbacAccessLevel::fromId
    )

    /**
     * The timestamp when the record was created.
     */
    val createdAt = datetime(
        name = "created_at"
    ).defaultExpression(defaultValue = CurrentDateTime)

    /**
     * The timestamp when the record was last updated.
     */
    val updatedAt = datetime(
        name = "updated_at"
    ).defaultExpression(defaultValue = CurrentDateTime)

    override val primaryKey = PrimaryKey(
        firstColumn = id,
        name = "pk_field_rule_id"
    )

    init {
        uniqueIndex(
            customIndexName = "uq_rbac_field_rule__resource_rule_id__field_name",
            columns = arrayOf(resourceRuleId, fieldName)
        )
    }
}
