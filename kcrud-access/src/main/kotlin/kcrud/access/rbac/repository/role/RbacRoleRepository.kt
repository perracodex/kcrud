/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.rbac.repository.role

import kcrud.access.rbac.model.role.RbacRole
import kcrud.access.rbac.model.role.RbacRoleRequest
import kcrud.access.rbac.repository.scope.IRbacScopeRuleRepository
import kcrud.base.database.schema.admin.actor.ActorTable
import kcrud.base.database.schema.admin.rbac.RbacFieldRuleTable
import kcrud.base.database.schema.admin.rbac.RbacRoleTable
import kcrud.base.database.schema.admin.rbac.RbacScopeRuleTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import kotlin.uuid.Uuid

/**
 * Implementation of [IRbacRoleRepository].
 * Handles the persistence of [RbacRole] data.
 *
 * @see IRbacRoleRepository
 */
internal class RbacRoleRepository(
    private val scopeRuleRepository: IRbacScopeRuleRepository
) : IRbacRoleRepository {

    override fun findById(roleId: Uuid): RbacRole? {
        return transaction {
            RbacRoleTable
                .leftJoin(RbacScopeRuleTable)
                .leftJoin(RbacFieldRuleTable)
                .selectAll()
                .where { RbacRoleTable.id eq roleId }
                .groupBy { it[RbacRoleTable.id] }
                .map { (_, rows) ->
                    RbacRole.from(roleId = roleId, rows = rows)
                }.singleOrNull()
        }
    }

    override fun findByActorId(actorId: Uuid): RbacRole? {
        return transaction {
            // Filter out the Actor table columns. Only include the RBAC columns.
            val columns: List<Column<*>> = listOf(
                RbacRoleTable.columns,
                RbacScopeRuleTable.columns,
                RbacFieldRuleTable.columns
            ).flatten()

            RbacRoleTable
                .innerJoin(ActorTable)
                .leftJoin(RbacScopeRuleTable)
                .leftJoin(RbacFieldRuleTable)
                .select(columns = columns)
                .where { ActorTable.id eq actorId }
                .groupBy { it[RbacRoleTable.id] }
                .map { (roleId, rows) ->
                    RbacRole.from(roleId = roleId, rows = rows)
                }.singleOrNull()
        }
    }

    override fun findAll(): List<RbacRole> {
        return transaction {
            RbacRoleTable
                .leftJoin(RbacScopeRuleTable)
                .leftJoin(RbacFieldRuleTable)
                .selectAll()
                .groupBy { it[RbacRoleTable.id] }
                .map { (roleId, rows) ->
                    RbacRole.from(roleId = roleId, rows = rows)
                }
        }
    }

    override fun create(roleRequest: RbacRoleRequest): RbacRole {
        return transaction {
            val roleId: Uuid = RbacRoleTable.insert { roleRow ->
                roleRow.mapRoleRequest(roleRequest = roleRequest)
            } get RbacRoleTable.id

            // If the role insert was successful, insert the scope rules.
            if (!roleRequest.scopeRules.isNullOrEmpty()) {
                scopeRuleRepository.replace(
                    roleId = roleId,
                    scopeRuleRequests = roleRequest.scopeRules
                )
            }

            findById(roleId = roleId)
                ?: throw IllegalStateException("New record not found.")
        }
    }

    override fun update(roleId: Uuid, roleRequest: RbacRoleRequest): RbacRole? {
        return transaction {
            val updateCount: Int = RbacRoleTable.update(
                where = {
                    RbacRoleTable.id eq roleId
                }
            ) { roleRow ->
                roleRow.mapRoleRequest(roleRequest = roleRequest)
            }

            // If the update was successful, recreate the scope rules.
            if (updateCount > 0) {
                scopeRuleRepository.replace(
                    roleId = roleId,
                    scopeRuleRequests = roleRequest.scopeRules
                )

                findById(roleId = roleId)
            } else {
                null
            }
        }
    }

    /**
     * Populates an SQL [UpdateBuilder] with data from an [RbacRoleRequest] instance,
     * so that it can be used to update or create a database record.
     */
    private fun UpdateBuilder<Int>.mapRoleRequest(roleRequest: RbacRoleRequest) {
        this[RbacRoleTable.role_name] = roleRequest.roleName.trim()
        this[RbacRoleTable.description] = roleRequest.description?.trim()
        this[RbacRoleTable.isSuper] = roleRequest.isSuper
    }
}
