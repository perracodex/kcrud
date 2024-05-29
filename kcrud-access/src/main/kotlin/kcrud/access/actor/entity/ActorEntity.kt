/*
 * Copyright (c) 2023-Present Perracodex. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.access.actor.entity

import kcrud.access.credential.CredentialService
import kcrud.access.rbac.entity.role.RbacRoleEntity
import kcrud.base.database.schema.admin.actor.ActorTable
import kcrud.base.env.SessionContext
import kcrud.base.persistence.entity.Meta
import org.jetbrains.exposed.sql.ResultRow
import java.util.*

/**
 * Represents a single Actor. An Actor is a user with a role and access to scopes.
 *
 * This entity is meant to be short-lived and not serialized, as it contains the Actor's password.
 * Instead, its details must be mapped to a [SessionContext] instance, while the password is automatically
 * cached by [CredentialService] at the server startup, where it is hashed and kept in-memory for
 * authentication purposes.
 *
 * @property id The Actor's unique id.
 * @property username The Actor's unique username.
 * @property password The unencrypted Actor's password.
 * @property role The associated role.
 * @property isLocked Whether the Actor is locked, so its role and associated rules are ignored, loosing all accesses.
 * @property meta The metadata of the record.
 */
data class ActorEntity(
    var id: UUID,
    val username: String,
    val password: String,
    val role: RbacRoleEntity,
    val isLocked: Boolean,
    val meta: Meta
) {
    companion object {
        /**
         * Maps a [ResultRow] to an [ActorEntity] instance.
         *
         * @param row The [ResultRow] to map.
         * @param role The associated [RbacRoleEntity] instance.
         * @return The mapped [ActorEntity] instance.
         */
        fun from(row: ResultRow, role: RbacRoleEntity): ActorEntity {
            return ActorEntity(
                id = row[ActorTable.id],
                username = row[ActorTable.username],
                password = row[ActorTable.password],
                role = role,
                isLocked = row[ActorTable.isLocked],
                meta = Meta.toEntity(row = row, table = ActorTable)
            )
        }
    }
}
