/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.rbac.repository.field

import kcrud.access.rbac.model.field.RbacFieldRuleRequest
import kcrud.core.database.schema.admin.rbac.RbacFieldRuleTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.statements.BatchInsertStatement
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.uuid.Uuid

/**
 * Implementation of [IRbacFieldRuleRepository].
 * Manages the persistence of [RbacFieldRuleRequest] instances.
 *
 * @see RbacFieldRuleRequest
 */
internal class RbacFieldRuleRepository : IRbacFieldRuleRepository {

    override fun replace(scopeRuleId: Uuid, requestList: List<RbacFieldRuleRequest>?): Int {
        return transaction {
            RbacFieldRuleTable.deleteWhere {
                RbacFieldRuleTable.scopeRuleId eq scopeRuleId
            }

            requestList?.takeIf { it.isNotEmpty() }?.let { requestList ->
                RbacFieldRuleTable.batchInsert(requestList) { scopeRule ->
                    this.toStatement(scopeRuleId = scopeRuleId, request = scopeRule)
                }.size
            } ?: 0
        }
    }

    /**
     * Populates an SQL [BatchInsertStatement] with data from an [RbacFieldRuleRequest] instance,
     * so that it can be used to update or create a database record.
     */
    private fun BatchInsertStatement.toStatement(scopeRuleId: Uuid, request: RbacFieldRuleRequest) {
        this[RbacFieldRuleTable.scopeRuleId] = scopeRuleId
        this[RbacFieldRuleTable.fieldName] = request.fieldName
        this[RbacFieldRuleTable.accessLevel] = request.accessLevel
    }
}
