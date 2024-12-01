/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.domain.rbac.repository.scope

import kcrud.access.domain.rbac.model.scope.RbacScopeRuleRequest
import kcrud.access.domain.rbac.repository.field.IRbacFieldRuleRepository
import kcrud.database.schema.admin.rbac.RbacScopeRuleTable
import kcrud.database.schema.admin.rbac.type.RbacScope
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.statements.BatchInsertStatement
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.uuid.Uuid

/**
 * Implementation of [IRbacScopeRuleRepository].
 * Responsible for managing [RbacScopeRuleRequest] data.
 *
 * @see [RbacScopeRuleRequest]
 */
internal class RbacScopeRuleRepository(
    private val fieldRuleRepository: IRbacFieldRuleRepository
) : IRbacScopeRuleRepository {

    override fun replace(roleId: Uuid, scopeRuleRequests: List<RbacScopeRuleRequest>?): Int {
        return transaction {
            RbacScopeRuleTable.deleteWhere {
                RbacScopeRuleTable.roleId eq roleId
            }

            scopeRuleRequests.takeUnless { it.isNullOrEmpty() }?.let { scopeRuleRequests ->
                // Batch insert new scope rules.
                val newScopeRules: List<ResultRow> = RbacScopeRuleTable.batchInsert(
                    data = scopeRuleRequests
                ) { scopeRuleRequest ->
                    this.toStatement(roleId = roleId, scopeRuleRequest = scopeRuleRequest)
                }

                // If the update was successful, recreate the field rules.
                newScopeRules.forEach { scopeRule ->
                    val newScopeRuleId: Uuid = scopeRule[RbacScopeRuleTable.id]
                    val rbacScope: RbacScope = scopeRule[RbacScopeRuleTable.scope]

                    // Find the field rules for the scope rule using the scope name.
                    // If the field rules are not empty, update the field rules.
                    scopeRuleRequests.firstOrNull { scopeRuleRequest ->
                        scopeRuleRequest.scope == rbacScope
                    }?.fieldRules?.also { fieldRuleRequests ->
                        fieldRuleRepository.replace(
                            scopeRuleId = newScopeRuleId,
                            requestList = fieldRuleRequests
                        )
                    }
                }

                newScopeRules.size
            } ?: 0
        }
    }

    /**
     * Populates an SQL [BatchInsertStatement] with data from an [RbacScopeRuleRequest] instance,
     * so that it can be used to update or create a database record.
     */
    private fun BatchInsertStatement.toStatement(roleId: Uuid, scopeRuleRequest: RbacScopeRuleRequest) {
        this[RbacScopeRuleTable.roleId] = roleId
        this[RbacScopeRuleTable.scope] = scopeRuleRequest.scope
        this[RbacScopeRuleTable.accessLevel] = scopeRuleRequest.accessLevel
    }
}
