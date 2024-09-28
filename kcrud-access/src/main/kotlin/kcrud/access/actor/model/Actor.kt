/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.actor.model

import kcrud.access.rbac.model.role.RbacRole
import kcrud.core.database.schema.admin.actor.ActorTable
import kcrud.core.persistence.model.Meta
import org.jetbrains.exposed.sql.ResultRow
import kotlin.uuid.Uuid

/**
 * Represents a single Actor. An Actor is a user with a role and access to scopes.
 *
 * This class does not contain the Actor's password, so the class can be cached in memory
 * without security concerns. If the password is needed, use [ActorCredentials] instead.
 *
 * @property id The Actor's unique id.
 * @property username The Actor's unique username.
 * @property role The associated role.
 * @property isLocked Whether the Actor is locked, so its role and associated rules are ignored, loosing all accesses.
 * @property meta The metadata of the record.
 *
 * @see [ActorCredentials]
 */
internal data class Actor(
    var id: Uuid,
    val username: String,
    val role: RbacRole,
    val isLocked: Boolean,
    val meta: Meta
) {
    companion object {
        /**
         * Maps a [ResultRow] to an [Actor] instance.
         *
         * @param row The [ResultRow] to map.
         * @param role The associated [RbacRole] instance.
         * @return The mapped [Actor] instance.
         */
        fun from(row: ResultRow, role: RbacRole): Actor {
            return Actor(
                id = row[ActorTable.id],
                username = row[ActorTable.username],
                role = role,
                isLocked = row[ActorTable.isLocked],
                meta = Meta.from(row = row, table = ActorTable)
            )
        }
    }
}
