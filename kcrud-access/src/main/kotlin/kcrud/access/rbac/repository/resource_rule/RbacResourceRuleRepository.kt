/*
 * Copyright (c) 2023-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.access.rbac.repository.resource_rule

import kcrud.access.rbac.entity.field_rule.RbacFieldRuleRequest
import kcrud.access.rbac.entity.resource_rule.RbacResourceRuleRequest
import kcrud.access.rbac.repository.field_rule.IRbacFieldRuleRepository
import kcrud.base.database.schema.admin.rbac.RbacResourceRuleTable
import kcrud.base.utils.DateTimeUtils
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.statements.BatchInsertStatement
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class RbacResourceRuleRepository(
    private val fieldRuleRepository: IRbacFieldRuleRepository
) : IRbacResourceRuleRepository {

    override fun replace(roleId: UUID, resourceRuleRequests: List<RbacResourceRuleRequest>?): Int {
        return transaction {
            RbacResourceRuleTable.deleteWhere {
                RbacResourceRuleTable.roleId eq roleId
            }

            var newRowCount = 0

            if (!resourceRuleRequests.isNullOrEmpty()) {
                val resourceRules: List<ResultRow> = RbacResourceRuleTable.batchInsert(
                    data = resourceRuleRequests
                ) { resourceRule ->
                    this.mapRuleRequest(roleId = roleId, resourceRuleRequest = resourceRule)
                }

                newRowCount = resourceRules.size

                // If the update was successful, recreate the field rules.
                if (newRowCount > 0) {
                    resourceRules.forEach { resourceRule ->

                        // Find the field rules for the resource rule using the resource name.
                        val fieldRuleRequest: List<RbacFieldRuleRequest>? = resourceRuleRequests.firstOrNull {
                            it.resource == resourceRule[RbacResourceRuleTable.resource]
                        }?.fieldRules

                        // If the field rules are not empty, update the field rules.
                        val newResourceRuleId: UUID = resourceRule[RbacResourceRuleTable.id]
                        fieldRuleRepository.replace(
                            resourceRuleId = newResourceRuleId,
                            requestList = fieldRuleRequest
                        )
                    }
                }
            }

            newRowCount
        }
    }

    /**
     * Populates an SQL [BatchInsertStatement] with data from an [RbacResourceRuleRequest] instance,
     * so that it can be used to update or create a database record.
     */
    private fun BatchInsertStatement.mapRuleRequest(roleId: UUID, resourceRuleRequest: RbacResourceRuleRequest) {
        this[RbacResourceRuleTable.roleId] = roleId
        this[RbacResourceRuleTable.resource] = resourceRuleRequest.resource
        this[RbacResourceRuleTable.accessLevel] = resourceRuleRequest.accessLevel
        this[RbacResourceRuleTable.updatedAt] = DateTimeUtils.currentUTCDateTime()
    }
}
