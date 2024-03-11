/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.database.schema.admin.rbac

import kcrud.base.database.schema.admin.rbac.types.RbacAccessLevel
import kcrud.base.database.schema.admin.rbac.types.RbacResource
import kcrud.base.persistence.utils.enumById
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

/**
 * Database table definition holding RBAC rules for a concrete [RbacRoleTable] record.
 *
 * A resource can be any concept: a database table, a REST endpoint, a UI element, etc.
 * Is up to the designer to define what a resource is, and act accordingly when its
 * associated RBAC rule is verified.
 *
 * @see RbacRoleTable
 */
object RbacResourceRuleTable : Table(name = "rbac_resource_rule") {
    /**
     * The unique id of the resource rule record.
     */
    val id = uuid(
        name = "resource_rule_id"
    ).autoGenerate()

    /**
     * The associated [RbacRoleTable] id.
     */
    val roleId = uuid(
        name = "role_id"
    ).references(
        fkName = "fk_rbac_resource_rule__role_id",
        ref = RbacRoleTable.id,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.RESTRICT
    )

    /**
     * The [RbacResource] the rule is meant to target.
     */
    val resource = enumerationByName(
        name = "resource",
        length = 64,
        klass = RbacResource::class
    )

    /**
     * The [RbacAccessLevel] representing the access level for the [RbacResource].
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
    ).defaultExpression(CurrentDateTime)

    /**
     * The timestamp when the record was last updated.
     */
    val updatedAt = datetime(
        name = "updated_at"
    ).defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(
        firstColumn = id,
        name = "pk_resource_rule_id"
    )

    init {
        uniqueIndex(
            customIndexName = "uq_rbac_resource_rule__role_id__resource",
            columns = arrayOf(roleId, resource)
        )
    }
}
