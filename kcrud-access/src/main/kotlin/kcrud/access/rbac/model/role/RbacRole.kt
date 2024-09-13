/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.rbac.model.role

import kcrud.access.rbac.model.field.RbacFieldRule
import kcrud.access.rbac.model.scope.RbacScopeRule
import kcrud.base.database.schema.admin.rbac.RbacFieldRuleTable
import kcrud.base.database.schema.admin.rbac.RbacRoleTable
import kcrud.base.database.schema.admin.rbac.RbacScopeRuleTable
import kcrud.base.persistence.model.Meta
import kcrud.base.persistence.serializers.SUuid
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow
import kotlin.uuid.Uuid

/**
 * Represents a single RBAC Role.
 *
 * @property id The unique id of the role record.
 * @property roleName The unique role name.
 * @property description Optional role description.
 * @property isSuper Whether this is a super-role, in which case it should have all permissions granted.
 * @property scopeRules The list of [RbacScopeRule] entries for the role.
 * @property meta The metadata of the record.
 */
@Serializable
public data class RbacRole(
    val id: SUuid,
    val roleName: String,
    val description: String?,
    val isSuper: Boolean,
    val scopeRules: List<RbacScopeRule>,
    val meta: Meta
) {
    internal companion object {
        /**
         * Maps a list of [ResultRow]s to a [RbacRole] instance.
         * Each row is expected to represent a different scope rule.
         *
         * @param roleId The id of the role.
         * @param rows The [ResultRow] list to map.
         * @return The mapped [RbacRole] instance.
         */
        fun from(roleId: Uuid, rows: List<ResultRow>): RbacRole {
            // Construct the child entries (if any).
            val scopeRules: List<RbacScopeRule> = rows
                .filter { it.getOrNull(RbacScopeRuleTable.id) != null }
                .distinctBy { it[RbacScopeRuleTable.id] }
                .map { scopeRuleRow ->
                    val fieldRuleRows: List<ResultRow> = rows.filter {
                        it[RbacFieldRuleTable.scopeRuleId] == scopeRuleRow[RbacScopeRuleTable.id]
                    }
                    val fieldRules: List<RbacFieldRule> = fieldRuleRows.map {
                        RbacFieldRule.from(row = it)
                    }
                    RbacScopeRule.from(row = scopeRuleRow, fieldRules = fieldRules)
                }

            // Use the first row as the role of the 1-N relationship,
            // as the rows come in a flattened format due to the SQL joins.
            val record: ResultRow = rows.first()

            return RbacRole(
                id = roleId,
                roleName = record[RbacRoleTable.role_name],
                description = record[RbacRoleTable.description],
                isSuper = record[RbacRoleTable.isSuper],
                scopeRules = scopeRules,
                meta = Meta.from(row = record, table = RbacRoleTable)
            )
        }
    }
}
