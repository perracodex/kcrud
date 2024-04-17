/*
 * Copyright (c) 2023-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.access.rbac.entity.role

import kcrud.access.rbac.entity.field_rule.RbacFieldRuleEntity
import kcrud.access.rbac.entity.scope_rule.RbacScopeRuleEntity
import kcrud.base.database.schema.admin.rbac.RbacFieldRuleTable
import kcrud.base.database.schema.admin.rbac.RbacRoleTable
import kcrud.base.database.schema.admin.rbac.RbacScopeRuleTable
import kcrud.base.persistence.entity.Meta
import kcrud.base.persistence.serializers.SUUID
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow
import java.util.*

/**
 * Represents a single RBAC Role.
 *
 * @property id The unique id of the role record.
 * @property roleName The unique role name.
 * @property description Optional role description.
 * @property isSuper Whether this is a super-role, in which case it should have all permissions granted.
 * @property scopeRules The list of [RbacScopeRuleEntity] entries for the role.
 * @property meta The metadata of the record.
 */
@Serializable
data class RbacRoleEntity(
    val id: SUUID,
    val roleName: String,
    val description: String?,
    val isSuper: Boolean,
    val scopeRules: List<RbacScopeRuleEntity>,
    val meta: Meta
) {
    companion object {
        fun from(roleId: UUID, rows: List<ResultRow>): RbacRoleEntity {
            // Construct the child entities (if any).
            val scopeRules: List<RbacScopeRuleEntity> = rows
                .filter { it.getOrNull(RbacScopeRuleTable.id) != null }
                .distinctBy { it[RbacScopeRuleTable.id] }
                .map { scopeRuleRow ->
                    val scopeId: UUID = scopeRuleRow[RbacScopeRuleTable.id]
                    val fieldRuleRows: List<ResultRow> = rows.filter { it[RbacFieldRuleTable.scopeRuleId] == scopeId }
                    val fieldRuleEntities: List<RbacFieldRuleEntity> = fieldRuleRows.map {
                        RbacFieldRuleEntity.from(row = it)
                    }
                    RbacScopeRuleEntity.from(row = scopeRuleRow, fieldRules = fieldRuleEntities)
                }

            // Use the first row as the role of the 1-N relationship,
            // as the rows come in a flattened format due to the SQL joins.
            val record: ResultRow = rows.first()

            return RbacRoleEntity(
                id = roleId,
                roleName = record[RbacRoleTable.role_name],
                description = record[RbacRoleTable.description],
                isSuper = record[RbacRoleTable.isSuper],
                scopeRules = scopeRules,
                meta = Meta.toEntity(row = record, table = RbacRoleTable)
            )
        }
    }
}
