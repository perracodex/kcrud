/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.database.schema.admin.actor

import kcrud.base.database.custom_columns.encryptedValidVarChar
import kcrud.base.database.schema.admin.rbac.RbacRoleTable
import kcrud.base.database.schema.base.TimestampedTable
import kcrud.base.persistence.utils.autoGenerate
import kcrud.base.persistence.utils.kotlinUuid
import kcrud.base.persistence.utils.references
import kcrud.base.security.utils.EncryptionUtils
import org.jetbrains.exposed.crypt.Encryptor
import org.jetbrains.exposed.crypt.encryptedVarchar
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import kotlin.uuid.Uuid

/**
 * Database table definition holding Actors.
 * An Actor is a user with a role and access to scopes.
 *
 * The password is encrypted with the [encryptedVarchar] exposed extension function.
 * It could be improved further by using our custom [encryptedValidVarChar] to add
 * some minimal validation constraints for all passwords.
 */
public object ActorTable : TimestampedTable(name = "actor") {
    private val encryptor: Encryptor = EncryptionUtils.getEncryptor(type = EncryptionUtils.Type.AT_REST)

    /**
     * The unique id of the Actor record.
     */
    public val id: Column<Uuid> = kotlinUuid(
        name = "actor_id"
    ).autoGenerate()

    /**
     * The actor's unique username.
     */
    public val username: Column<String> = varchar(
        name = "username",
        length = 16
    )

    /**
     * The actor's encrypted password.
     */
    public val password: Column<String> = encryptedVarchar(
        name = "password",
        cipherTextLength = encryptor.maxColLength(inputByteSize = 128),
        encryptor = encryptor
    )

    /**
     * The associated [RbacRoleTable] id.
     */
    public val roleId: Column<Uuid> = kotlinUuid(
        name = "role_id"
    ).references(
        ref = RbacRoleTable.id,
        onDelete = ReferenceOption.RESTRICT,
        onUpdate = ReferenceOption.RESTRICT,
        fkName = "fk_rbac_role__role_id"
    )

    /**
     * Whether the Actor is locked and therefore has full restricted access.
     */
    public val isLocked: Column<Boolean> = bool(
        name = "is_locked"
    )

    override val primaryKey: Table.PrimaryKey = PrimaryKey(
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
