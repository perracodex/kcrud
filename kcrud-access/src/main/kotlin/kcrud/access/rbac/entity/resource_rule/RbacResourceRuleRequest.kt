/*
 * Copyright (c) 2023-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.access.rbac.entity.resource_rule

import kcrud.access.rbac.entity.field_rule.RbacFieldRuleRequest
import kcrud.base.database.schema.admin.rbac.types.RbacAccessLevel
import kcrud.base.database.schema.admin.rbac.types.RbacResource
import kotlinx.serialization.Serializable

/**
 * Entity to create a RBAC resource rule.
 * Resource rules are never updated, only re-created and deleted.
 * Therefore, when updating an RBAC role, all its associated resource rules are deleted and re-created.
 *
 * A resource can be any concept: a database table, a REST endpoint, a UI element, etc.
 * Is up to the designer to define what a resource is, and act accordingly when its
 * associated RBAC rule is verified.
 *
 * @property resource The [RbacResource] being targeted.
 * @property accessLevel The required [RbacAccessLevel] for the [RbacResource].
 * @property fieldRules The list of [RbacFieldRuleRequest] for the resource. Or null if there are no field rules.
 */
@Serializable
data class RbacResourceRuleRequest(
    val resource: RbacResource,
    val accessLevel: RbacAccessLevel,
    val fieldRules: List<RbacFieldRuleRequest>? = null
)
