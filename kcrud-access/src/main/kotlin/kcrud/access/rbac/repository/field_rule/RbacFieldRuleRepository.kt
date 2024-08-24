/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.rbac.repository.field_rule

import kcrud.access.rbac.entity.field_rule.RbacFieldRuleRequest
import kcrud.base.database.schema.admin.rbac.RbacFieldRuleTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.statements.BatchInsertStatement
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

/**
 * Implementation of [IRbacFieldRuleRepository].
 * Manages the persistence of [RbacFieldRuleRequest] instances.
 *
 * @see RbacFieldRuleRequest
 */
class RbacFieldRuleRepository : IRbacFieldRuleRepository {

    override fun replace(scopeRuleId: Uuid, requestList: List<RbacFieldRuleRequest>?): Int {
        return transaction {
            RbacFieldRuleTable.deleteWhere {
                RbacFieldRuleTable.scopeRuleId eq scopeRuleId.toJavaUuid()
            }

            var newRowCount = 0

            if (!requestList.isNullOrEmpty()) {
                val newRows: List<ResultRow> = RbacFieldRuleTable.batchInsert(
                    data = requestList
                ) { scopeRule ->
                    this.mapRuleRequest(scopeRuleId = scopeRuleId, request = scopeRule)
                }

                newRowCount = newRows.size
            }

            newRowCount
        }
    }

    /**
     * Populates an SQL [BatchInsertStatement] with data from an [RbacFieldRuleRequest] instance,
     * so that it can be used to update or create a database record.
     */
    private fun BatchInsertStatement.mapRuleRequest(scopeRuleId: Uuid, request: RbacFieldRuleRequest) {
        this[RbacFieldRuleTable.scopeRuleId] = scopeRuleId.toJavaUuid()
        this[RbacFieldRuleTable.fieldName] = request.fieldName
        this[RbacFieldRuleTable.accessLevel] = request.accessLevel
    }
}
