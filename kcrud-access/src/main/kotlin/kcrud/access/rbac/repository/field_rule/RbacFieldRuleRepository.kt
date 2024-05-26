/*
 * Copyright (c) 2023-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.access.rbac.repository.field_rule

import kcrud.access.rbac.entity.field_rule.RbacFieldRuleRequest
import kcrud.base.database.schema.admin.rbac.RbacFieldRuleTable
import kcrud.base.utils.DateTimeUtils
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.statements.BatchInsertStatement
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

/**
 * Implementation of [IRbacFieldRuleRepository].
 * Manages the persistence of [RbacFieldRuleRequest] instances.
 *
 * @see RbacFieldRuleRequest
 */
class RbacFieldRuleRepository : IRbacFieldRuleRepository {

    override fun replace(scopeRuleId: UUID, requestList: List<RbacFieldRuleRequest>?): Int {
        return transaction {
            RbacFieldRuleTable.deleteWhere {
                RbacFieldRuleTable.scopeRuleId eq scopeRuleId
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
    private fun BatchInsertStatement.mapRuleRequest(scopeRuleId: UUID, request: RbacFieldRuleRequest) {
        this[RbacFieldRuleTable.scopeRuleId] = scopeRuleId
        this[RbacFieldRuleTable.fieldName] = request.fieldName
        this[RbacFieldRuleTable.accessLevel] = request.accessLevel
        this[RbacFieldRuleTable.updatedAt] = DateTimeUtils.currentUTCDateTime()
    }
}
