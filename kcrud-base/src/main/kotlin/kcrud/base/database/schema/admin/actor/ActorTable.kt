/*
 * Copyright (c) 2024-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.database.schema.admin.actor

import kcrud.base.database.custom_columns.encryptedValidVarChar
import kcrud.base.database.schema.admin.rbac.RbacRoleTable
import kcrud.base.database.schema.base.TimestampedTable
import kcrud.base.security.utils.EncryptionUtils
import org.jetbrains.exposed.crypt.Encryptor
import org.jetbrains.exposed.crypt.encryptedVarchar
import org.jetbrains.exposed.sql.ReferenceOption

/**
 * Database table definition holding Actors.
 * An Actor is a user with a role and access to resources.
 *
 * The password is encrypted with the [encryptedVarchar] exposed extension function.
 * It could be improved further by using our custom [encryptedValidVarChar] to add
 * some minimal validation constraints for all passwords.
 */
object ActorTable : TimestampedTable(name = "actor") {
    private val encryptor: Encryptor = EncryptionUtils.getEncryptor()

    /**
     * The unique id of the Actor record.
     */
    val id = uuid(
        name = "actor_id"
    ).autoGenerate()

    /**
     * The actor's unique username.
     */
    val username = varchar(
        name = "username",
        length = 16
    )

    /**
     * The actor's encrypted password.
     */
    val password = encryptedVarchar(
        name = "password",
        cipherTextLength = encryptor.maxColLength(inputByteSize = 128),
        encryptor = encryptor
    )

    /**
     * The associated [RbacRoleTable] id.
     */
    val roleId = uuid(
        name = "role_id"
    ).references(
        fkName = "fk_rbac_role__role_id",
        ref = RbacRoleTable.id,
        onDelete = ReferenceOption.RESTRICT,
        onUpdate = ReferenceOption.RESTRICT
    )

    /**
     * Whether the Actor is locked and therefore has full restricted access.
     */
    val isLocked = bool(
        name = "is_locked"
    )

    override val primaryKey = PrimaryKey(
        firstColumn = id,
        name = "pk_actor_id"
    )

    init {
        uniqueIndex(
            customIndexName = "uq_actor__username",
            columns = arrayOf(username)
        )
    }
}
