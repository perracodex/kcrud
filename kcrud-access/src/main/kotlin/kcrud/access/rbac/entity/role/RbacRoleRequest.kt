/*
 * Copyright (c) 2023-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.access.rbac.entity.role

import kcrud.access.rbac.entity.resource_rule.RbacResourceRuleEntity
import kcrud.access.rbac.entity.resource_rule.RbacResourceRuleRequest
import kotlinx.serialization.Serializable

/**
 * Represents the request to create/update a concrete RBAC Role.
 *
 * @property roleName The unique role name.
 * @property description Optional description of the role.
 * @property isSuper Whether this is a super-role, in which case it has all permissions granted.
 * @property resourceRules The list of [RbacResourceRuleEntity] entries for the role.
 */
@Serializable
data class RbacRoleRequest(
    val roleName: String,
    val description: String? = null,
    val isSuper: Boolean,
    val resourceRules: List<RbacResourceRuleRequest>? = null
)
