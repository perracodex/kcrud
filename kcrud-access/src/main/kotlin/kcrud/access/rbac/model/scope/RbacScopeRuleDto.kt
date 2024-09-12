/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.rbac.model.scope

import kcrud.access.rbac.model.field.RbacFieldRuleDto
import kcrud.access.rbac.model.role.RbacRoleDto
import kcrud.base.database.schema.admin.rbac.RbacScopeRuleTable
import kcrud.base.database.schema.admin.rbac.types.RbacAccessLevel
import kcrud.base.database.schema.admin.rbac.types.RbacScope
import kcrud.base.persistence.model.Meta
import kcrud.base.persistence.serializers.UuidS
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow

/**
 * Represents a single RBAC scope rule for a concrete [RbacRoleDto].
 *
 * A scope can be any concept: a database table, a REST endpoint, a UI element, etc.
 * Is up to the designer to define what a scope is, and act accordingly when its
 * associated RBAC rule is verified.
 *
 * @property id The unique id of the scope rule record.
 * @property roleId The associated parent [RbacRoleDto] id.
 * @property scope The [RbacScope] to which the scope rule belong.
 * @property accessLevel The required [RbacAccessLevel] for the [RbacScope].
 * @property fieldRules Optional list of [RbacFieldRuleDto] associated with the scope rule.
 * @property meta The metadata of the record.
 */
@Serializable
public data class RbacScopeRuleDto(
    val id: UuidS,
    val roleId: UuidS,
    val scope: RbacScope,
    val accessLevel: RbacAccessLevel,
    val fieldRules: List<RbacFieldRuleDto>?,
    val meta: Meta
) {
    public companion object {
        /**
         * Maps a [ResultRow] to a [RbacScopeRuleDto] instance.
         *
         * @param row The [ResultRow] to map.
         * @param fieldRules The list of [RbacFieldRuleDto] to associate with the [RbacScopeRuleDto].
         * @return The mapped [RbacScopeRuleDto] instance.
         */
        public fun from(row: ResultRow, fieldRules: List<RbacFieldRuleDto>): RbacScopeRuleDto {
            return RbacScopeRuleDto(
                id = row[RbacScopeRuleTable.id],
                roleId = row[RbacScopeRuleTable.roleId],
                scope = row[RbacScopeRuleTable.scope],
                accessLevel = row[RbacScopeRuleTable.accessLevel],
                fieldRules = fieldRules,
                meta = Meta.from(row = row, table = RbacScopeRuleTable)
            )
        }
    }
}
