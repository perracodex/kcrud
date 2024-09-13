/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.rbac.model.scope

import kcrud.access.rbac.model.field.RbacFieldRule
import kcrud.access.rbac.model.role.RbacRole
import kcrud.base.database.schema.admin.rbac.RbacScopeRuleTable
import kcrud.base.database.schema.admin.rbac.types.RbacAccessLevel
import kcrud.base.database.schema.admin.rbac.types.RbacScope
import kcrud.base.persistence.model.Meta
import kcrud.base.persistence.serializers.SUuid
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow

/**
 * Represents a single RBAC scope rule for a concrete [RbacRole].
 *
 * A scope can be any concept: a database table, a REST endpoint, a UI element, etc.
 * Is up to the designer to define what a scope is, and act accordingly when its
 * associated RBAC rule is verified.
 *
 * @property id The unique id of the scope rule record.
 * @property roleId The associated parent [RbacRole] id.
 * @property scope The [RbacScope] to which the scope rule belong.
 * @property accessLevel The required [RbacAccessLevel] for the [RbacScope].
 * @property fieldRules Optional list of [RbacFieldRule] associated with the scope rule.
 * @property meta The metadata of the record.
 */
@Serializable
public data class RbacScopeRule(
    val id: SUuid,
    val roleId: SUuid,
    val scope: RbacScope,
    val accessLevel: RbacAccessLevel,
    val fieldRules: List<RbacFieldRule>?,
    val meta: Meta
) {
    public companion object {
        /**
         * Maps a [ResultRow] to a [RbacScopeRule] instance.
         *
         * @param row The [ResultRow] to map.
         * @param fieldRules The list of [RbacFieldRule] to associate with the [RbacScopeRule].
         * @return The mapped [RbacScopeRule] instance.
         */
        public fun from(row: ResultRow, fieldRules: List<RbacFieldRule>): RbacScopeRule {
            return RbacScopeRule(
                id = row[RbacScopeRuleTable.id],
                roleId = row[RbacScopeRuleTable.roleId],
                scope = row[RbacScopeRuleTable.scope],
                accessLevel = row[RbacScopeRuleTable.accessLevel],
                fieldRules = fieldRules.takeIf { it.isNotEmpty() },
                meta = Meta.from(row = row, table = RbacScopeRuleTable)
            )
        }
    }
}
