/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.rbac.repository.role

import kcrud.access.rbac.entity.role.RbacRoleEntity
import kcrud.access.rbac.entity.role.RbacRoleRequest
import kotlin.uuid.Uuid

/**
 * Repository for [RbacRoleEntity] data.
 *
 * @see RbacRoleRequest
 */
internal interface IRbacRoleRepository {

    /**
     * Finds the [RbacRoleEntity] for the given [roleId]
     *
     * @param roleId The unique id of the [RbacRoleEntity] to find.
     * @return The [RbacRoleEntity] for the given [roleId], or null if not found.
     */
    fun findById(roleId: Uuid): RbacRoleEntity?

    /**
     * Finds the [RbacRoleEntity] for the given [actorId]
     *
     * @param actorId The id of the actor to which the role is associated.
     * @return The [RbacRoleEntity] for the given [actorId], or null if it doesn't exist.
     */
    fun findByActorId(actorId: Uuid): RbacRoleEntity?

    /**
     * Finds all existing [RbacRoleEntity] entries.
     *
     * @return List of all [RbacRoleEntity] entries.
     */
    fun findAll(): List<RbacRoleEntity>

    /**
     * Creates a new [RbacRoleEntity].
     *
     * @param roleRequest The [RbacRoleRequest] to create the [RbacRoleEntity] from.
     * @return The id of the newly created [RbacRoleEntity].
     */
    fun create(roleRequest: RbacRoleRequest): Uuid

    /**
     * Updates an existing [RbacRoleEntity] for the given [roleId].
     *
     * The current existing roles will be replaced by the new ones.
     *
     * @param roleId The id of the [RbacRoleEntity] to update.
     * @param roleRequest The [RbacRoleRequest] to update the [RbacRoleEntity] from.
     * @return The number of rows updated.
     */
    fun update(roleId: Uuid, roleRequest: RbacRoleRequest): Int
}
