/*
 * Copyright (c) 2023-Present Perracodex. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.access.actor.repository

import kcrud.access.actor.entity.ActorEntity
import kcrud.access.actor.entity.ActorRequest
import kcrud.access.rbac.entity.role.RbacRoleEntity
import kcrud.access.rbac.repository.role.IRbacRoleRepository
import kcrud.base.database.schema.admin.actor.ActorTable
import kcrud.base.utils.DateTimeUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.*

/**
 * Implementation of [IActorRepository].
 * Responsible for managing [ActorEntity] data.
 */
class ActorRepository(private val roleRepository: IRbacRoleRepository) : IActorRepository {

    override suspend fun findByUsername(username: String): ActorEntity? {
        return transaction {
            ActorTable.selectAll().where {
                ActorTable.username.eq(username)
            }.singleOrNull()?.let { resultRow ->
                val actorId: UUID = resultRow[ActorTable.id]
                val role: RbacRoleEntity = roleRepository.findByActorId(actorId = actorId)!!
                ActorEntity.from(row = resultRow, role = role)
            }
        }
    }

    override suspend fun findAll(): List<ActorEntity> {
        return transaction {
            ActorTable.selectAll().map { resultRow ->
                val actorId: UUID = resultRow[ActorTable.id]
                val role: RbacRoleEntity = roleRepository.findByActorId(actorId = actorId)!!
                ActorEntity.from(row = resultRow, role = role)
            }
        }
    }

    override suspend fun findById(actorId: UUID): ActorEntity? {
        return transaction {
            ActorTable.selectAll().where {
                ActorTable.id eq actorId
            }.singleOrNull()?.let { resultRow ->
                val role: RbacRoleEntity = roleRepository.findByActorId(actorId = actorId)!!
                ActorEntity.from(row = resultRow, role = role)
            }
        }
    }

    override suspend fun create(actorRequest: ActorRequest): UUID {
        return transaction {
            ActorTable.insert { actorRow ->
                actorRow.mapActorRequest(request = actorRequest, withTimestamp = false)
            } get ActorTable.id
        }
    }

    override suspend fun update(actorId: UUID, actorRequest: ActorRequest): Int {
        return transaction {
            ActorTable.update(
                where = {
                    ActorTable.id eq actorId
                }
            ) { actorRow ->
                actorRow.mapActorRequest(request = actorRequest)
            }
        }
    }

    override suspend fun setLockedState(actorId: UUID, isLocked: Boolean) {
        transaction {
            ActorTable.update(
                where = {
                    ActorTable.id eq actorId
                }
            ) {
                it[ActorTable.isLocked] = isLocked
            }
        }
    }

    override suspend fun actorsExist(usernames: List<String>?): Boolean {
        return transaction {
            if (usernames.isNullOrEmpty()) {
                ActorTable.select(
                    column = ActorTable.id
                ).count() > 0L
            } else {
                val targetUserNames: List<String> = usernames.map { it.lowercase() }

                ActorTable.select(
                    column = ActorTable.id
                ).where {
                    ActorTable.username.lowerCase() inList targetUserNames
                }.count() > 0L
            }
        }
    }

    /**
     * Populates an SQL [UpdateBuilder] with data from an [ActorRequest] instance,
     * so that it can be used to update or create a database record.
     */
    private fun UpdateBuilder<Int>.mapActorRequest(request: ActorRequest, withTimestamp: Boolean = true) {
        this[ActorTable.username] = request.username.lowercase()
        this[ActorTable.password] = request.password
        this[ActorTable.roleId] = request.roleId
        this[ActorTable.isLocked] = request.isLocked
        if (withTimestamp) this[ActorTable.updatedAt] = DateTimeUtils.currentUTCDateTime()
    }
}
