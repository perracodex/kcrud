/*
 * Copyright (c) 2023-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.access.rbac.entity.resource_rule

import kcrud.access.rbac.entity.field_rule.RbacFieldRuleEntity
import kcrud.access.rbac.entity.role.RbacRoleEntity
import kcrud.base.database.schema.admin.rbac.RbacResourceRuleTable
import kcrud.base.database.schema.admin.rbac.types.RbacAccessLevel
import kcrud.base.database.schema.admin.rbac.types.RbacResource
import kcrud.base.persistence.entity.Meta
import kcrud.base.persistence.serializers.SUUID
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow

/**
 * Represents a single RBAC resource rule for a concrete [RbacRoleEntity].
 *
 * A resource can be any concept: a database table, a REST endpoint, a UI element, etc.
 * Is up to the designer to define what a resource is, and act accordingly when its
 * associated RBAC rule is verified.
 *
 * @property id The unique id of the resource rule record.
 * @property roleId The associated parent [RbacRoleEntity] id.
 * @property resource The [RbacResource] to which the resource rule belong.
 * @property accessLevel The required [RbacAccessLevel] for the [RbacResource].
 * @property fieldRules Optional list of [RbacFieldRuleEntity] associated with the resource rule.
 * @property meta The metadata of the record.
 */
@Serializable
data class RbacResourceRuleEntity(
    val id: SUUID,
    val roleId: SUUID,
    val resource: RbacResource,
    val accessLevel: RbacAccessLevel,
    val fieldRules: List<RbacFieldRuleEntity>?,
    val meta: Meta
) {
    companion object {
        fun from(row: ResultRow, fieldRules: List<RbacFieldRuleEntity>): RbacResourceRuleEntity {
            return RbacResourceRuleEntity(
                id = row[RbacResourceRuleTable.id],
                roleId = row[RbacResourceRuleTable.roleId],
                resource = row[RbacResourceRuleTable.resource],
                accessLevel = row[RbacResourceRuleTable.accessLevel],
                fieldRules = fieldRules,
                meta = Meta.toEntity(row = row, table = RbacResourceRuleTable)
            )
        }
    }
}
