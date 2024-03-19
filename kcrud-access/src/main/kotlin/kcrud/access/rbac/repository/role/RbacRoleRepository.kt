/*
 * Copyright (c) 2023-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.access.rbac.repository.role

import kcrud.access.rbac.entity.role.RbacRoleEntity
import kcrud.access.rbac.entity.role.RbacRoleRequest
import kcrud.access.rbac.repository.resource_rule.IRbacResourceRuleRepository
import kcrud.base.database.schema.admin.actor.ActorTable
import kcrud.base.database.schema.admin.rbac.RbacFieldRuleTable
import kcrud.base.database.schema.admin.rbac.RbacResourceRuleTable
import kcrud.base.database.schema.admin.rbac.RbacRoleTable
import kcrud.base.utils.DateTimeUtils
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.*

class RbacRoleRepository(
    private val resourceRuleRepository: IRbacResourceRuleRepository
) : IRbacRoleRepository {

    override fun findById(roleId: UUID): RbacRoleEntity? {
        return transaction {
            RbacRoleTable
                .leftJoin(otherTable = RbacResourceRuleTable)
                .leftJoin(otherTable = RbacFieldRuleTable)
                .selectAll()
                .where { RbacRoleTable.id eq roleId }
                .groupBy { it[RbacRoleTable.id] }
                .map { (roleId, rows) ->
                    RbacRoleEntity.from(roleId = roleId, rows = rows)
                }.singleOrNull()
        }
    }

    override fun findByActorId(actorId: UUID): RbacRoleEntity? {
        return transaction {
            // Filter out the Actor table columns. Only include the RBAC columns.
            val columns: List<Column<*>> = listOf(
                RbacRoleTable.columns,
                RbacResourceRuleTable.columns,
                RbacFieldRuleTable.columns
            ).flatten()

            RbacRoleTable
                .innerJoin(otherTable = ActorTable)
                .leftJoin(otherTable = RbacResourceRuleTable)
                .leftJoin(otherTable = RbacFieldRuleTable)
                .select(columns = columns)
                .where { ActorTable.id eq actorId }
                .groupBy { it[RbacRoleTable.id] }
                .map { (roleId, rows) ->
                    RbacRoleEntity.from(roleId = roleId, rows = rows)
                }.singleOrNull()
        }
    }

    override fun findAll(): List<RbacRoleEntity> {
        return transaction {
            RbacRoleTable
                .leftJoin(otherTable = RbacResourceRuleTable)
                .leftJoin(otherTable = RbacFieldRuleTable)
                .selectAll()
                .groupBy { it[RbacRoleTable.id] }
                .map { (roleId, rows) ->
                    RbacRoleEntity.from(roleId = roleId, rows = rows)
                }
        }
    }

    override fun create(roleRequest: RbacRoleRequest): UUID {
        return transaction {
            val roleId: UUID = RbacRoleTable.insert { roleRow ->
                roleRow.mapRoleRequest(roleRequest = roleRequest, withTimestamp = false)
            } get RbacRoleTable.id

            // If the role insert was successful, insert the resource rules.
            if (!roleRequest.resourceRules.isNullOrEmpty()) {
                resourceRuleRepository.replace(
                    roleId = roleId,
                    resourceRuleRequests = roleRequest.resourceRules
                )
            }

            roleId
        }
    }

    override fun update(roleId: UUID, roleRequest: RbacRoleRequest): Int {
        return transaction {
            val updateCount: Int = RbacRoleTable.update(
                where = {
                    RbacRoleTable.id eq roleId
                }
            ) { roleRow ->
                roleRow.mapRoleRequest(roleRequest = roleRequest)
            }

            // If the update was successful, recreate the resource rules.
            if (updateCount > 0) {
                resourceRuleRepository.replace(
                    roleId = roleId,
                    resourceRuleRequests = roleRequest.resourceRules
                )
            }

            updateCount
        }
    }

    /**
     * Populates an SQL [UpdateBuilder] with data from an [RbacRoleRequest] instance,
     * so that it can be used to update or create a database record.
     */
    private fun UpdateBuilder<Int>.mapRoleRequest(roleRequest: RbacRoleRequest, withTimestamp: Boolean = true) {
        this[RbacRoleTable.role_name] = roleRequest.roleName.trim()
        this[RbacRoleTable.description] = roleRequest.description?.trim()
        this[RbacRoleTable.isSuper] = roleRequest.isSuper
        this[RbacRoleTable.updatedAt] = DateTimeUtils.currentUTCDateTime()
        if (withTimestamp) this[RbacRoleTable.updatedAt] = DateTimeUtils.currentUTCDateTime()
    }
}
