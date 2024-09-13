/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.actor.repository

import kcrud.access.actor.model.Actor
import kcrud.access.actor.model.ActorRequest
import kcrud.access.rbac.model.role.RbacRole
import kcrud.access.rbac.repository.role.IRbacRoleRepository
import kcrud.base.database.schema.admin.actor.ActorTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import kotlin.uuid.Uuid

/**
 * Implementation of [IActorRepository].
 * Responsible for managing [Actor] data.
 */
internal class ActorRepository(private val roleRepository: IRbacRoleRepository) : IActorRepository {

    override suspend fun findByUsername(username: String): Actor? {
        return transaction {
            ActorTable.selectAll().where {
                ActorTable.username.eq(username)
            }.singleOrNull()?.let { resultRow ->
                val actorId: Uuid = resultRow[ActorTable.id]
                val role: RbacRole = roleRepository.findByActorId(actorId = actorId)!!
                Actor.from(row = resultRow, role = role)
            }
        }
    }

    override suspend fun findAll(): List<Actor> {
        return transaction {
            ActorTable.selectAll().map { resultRow ->
                val actorId: Uuid = resultRow[ActorTable.id]
                val role: RbacRole = roleRepository.findByActorId(actorId = actorId)!!
                Actor.from(row = resultRow, role = role)
            }
        }
    }

    override suspend fun findById(actorId: Uuid): Actor? {
        return transaction {
            ActorTable.selectAll().where {
                ActorTable.id eq actorId
            }.singleOrNull()?.let { resultRow ->
                val role: RbacRole = roleRepository.findByActorId(actorId = actorId)!!
                Actor.from(row = resultRow, role = role)
            }
        }
    }

    override suspend fun create(actorRequest: ActorRequest): Uuid {
        return transaction {
            ActorTable.insert { actorRow ->
                actorRow.mapActorRequest(request = actorRequest)
            } get ActorTable.id
        }
    }

    override suspend fun update(actorId: Uuid, actorRequest: ActorRequest): Int {
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

    override suspend fun setLockedState(actorId: Uuid, isLocked: Boolean) {
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
    private fun UpdateBuilder<Int>.mapActorRequest(request: ActorRequest) {
        this[ActorTable.username] = request.username.lowercase()
        this[ActorTable.password] = request.password
        this[ActorTable.roleId] = request.roleId
        this[ActorTable.isLocked] = request.isLocked
    }
}
