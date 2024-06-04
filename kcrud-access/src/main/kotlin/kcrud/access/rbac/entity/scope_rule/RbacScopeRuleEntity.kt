/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.rbac.entity.scope_rule

import kcrud.access.rbac.entity.field_rule.RbacFieldRuleEntity
import kcrud.access.rbac.entity.role.RbacRoleEntity
import kcrud.base.database.schema.admin.rbac.RbacScopeRuleTable
import kcrud.base.database.schema.admin.rbac.types.RbacAccessLevel
import kcrud.base.database.schema.admin.rbac.types.RbacScope
import kcrud.base.persistence.entity.Meta
import kcrud.base.persistence.serializers.SUUID
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow

/**
 * Represents a single RBAC scope rule for a concrete [RbacRoleEntity].
 *
 * A scope can be any concept: a database table, a REST endpoint, a UI element, etc.
 * Is up to the designer to define what a scope is, and act accordingly when its
 * associated RBAC rule is verified.
 *
 * @property id The unique id of the scope rule record.
 * @property roleId The associated parent [RbacRoleEntity] id.
 * @property scope The [RbacScope] to which the scope rule belong.
 * @property accessLevel The required [RbacAccessLevel] for the [RbacScope].
 * @property fieldRules Optional list of [RbacFieldRuleEntity] associated with the scope rule.
 * @property meta The metadata of the record.
 */
@Serializable
data class RbacScopeRuleEntity(
    val id: SUUID,
    val roleId: SUUID,
    val scope: RbacScope,
    val accessLevel: RbacAccessLevel,
    val fieldRules: List<RbacFieldRuleEntity>?,
    val meta: Meta
) {
    companion object {
        /**
         * Maps a [ResultRow] to a [RbacScopeRuleEntity] instance.
         *
         * @param row The [ResultRow] to map.
         * @param fieldRules The list of [RbacFieldRuleEntity] to associate with the [RbacScopeRuleEntity].
         * @return The mapped [RbacScopeRuleEntity] instance.
         */
        fun from(row: ResultRow, fieldRules: List<RbacFieldRuleEntity>): RbacScopeRuleEntity {
            return RbacScopeRuleEntity(
                id = row[RbacScopeRuleTable.id],
                roleId = row[RbacScopeRuleTable.roleId],
                scope = row[RbacScopeRuleTable.scope],
                accessLevel = row[RbacScopeRuleTable.accessLevel],
                fieldRules = fieldRules,
                meta = Meta.toEntity(row = row, table = RbacScopeRuleTable)
            )
        }
    }
}
