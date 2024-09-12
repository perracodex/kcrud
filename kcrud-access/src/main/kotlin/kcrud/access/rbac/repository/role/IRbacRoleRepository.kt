/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.rbac.repository.role

import kcrud.access.rbac.model.role.RbacRoleDto
import kcrud.access.rbac.model.role.RbacRoleRequest
import kotlin.uuid.Uuid

/**
 * Repository for [RbacRoleDto] data.
 *
 * @see RbacRoleRequest
 */
internal interface IRbacRoleRepository {

    /**
     * Finds the [RbacRoleDto] for the given [roleId]
     *
     * @param roleId The unique id of the [RbacRoleDto] to find.
     * @return The [RbacRoleDto] for the given [roleId], or null if not found.
     */
    fun findById(roleId: Uuid): RbacRoleDto?

    /**
     * Finds the [RbacRoleDto] for the given [actorId]
     *
     * @param actorId The id of the actor to which the role is associated.
     * @return The [RbacRoleDto] for the given [actorId], or null if it doesn't exist.
     */
    fun findByActorId(actorId: Uuid): RbacRoleDto?

    /**
     * Finds all existing [RbacRoleDto] entries.
     *
     * @return List of all [RbacRoleDto] entries.
     */
    fun findAll(): List<RbacRoleDto>

    /**
     * Creates a new [RbacRoleDto].
     *
     * @param roleRequest The [RbacRoleRequest] to create the [RbacRoleDto] from.
     * @return The id of the newly created [RbacRoleDto].
     */
    fun create(roleRequest: RbacRoleRequest): Uuid

    /**
     * Updates an existing [RbacRoleDto] for the given [roleId].
     *
     * The current existing roles will be replaced by the new ones.
     *
     * @param roleId The id of the [RbacRoleDto] to update.
     * @param roleRequest The [RbacRoleRequest] to update the [RbacRoleDto] from.
     * @return The number of rows updated.
     */
    fun update(roleId: Uuid, roleRequest: RbacRoleRequest): Int
}
