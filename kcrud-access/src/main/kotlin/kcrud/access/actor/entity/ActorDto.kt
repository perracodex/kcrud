/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.actor.entity

import kcrud.access.credential.CredentialService
import kcrud.access.rbac.entity.role.RbacRoleDto
import kcrud.base.database.schema.admin.actor.ActorTable
import kcrud.base.env.SessionContext
import kcrud.base.persistence.entity.Meta
import org.jetbrains.exposed.sql.ResultRow
import kotlin.uuid.Uuid

/**
 * Represents a single Actor. An Actor is a user with a role and access to scopes.
 *
 * This data is meant to be short-lived and not serialized, as it contains the Actor's password.
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
internal data class ActorDto(
    var id: Uuid,
    val username: String,
    val password: String,
    val role: RbacRoleDto,
    val isLocked: Boolean,
    val meta: Meta
) {
    companion object {
        /**
         * Maps a [ResultRow] to an [ActorDto] instance.
         *
         * @param row The [ResultRow] to map.
         * @param role The associated [RbacRoleDto] instance.
         * @return The mapped [ActorDto] instance.
         */
        fun from(row: ResultRow, role: RbacRoleDto): ActorDto {
            return ActorDto(
                id = row[ActorTable.id],
                username = row[ActorTable.username],
                password = row[ActorTable.password],
                role = role,
                isLocked = row[ActorTable.isLocked],
                meta = Meta.from(row = row, table = ActorTable)
            )
        }
    }
}
