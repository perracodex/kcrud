/*
 * Copyright (c) 2023-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.access.rbac.entity.field_rule

import kcrud.base.database.schema.admin.rbac.types.RbacAccessLevel
import kotlinx.serialization.Serializable

/**
 * Entity to create a RBAC field rule.
 * Field rules are never updated, only re-created and deleted.
 * So, when updating a resource rule, all field rules are deleted and re-created.
 *
 * @property fieldName The name of the field being targeted.
 * @property accessLevel The field [RbacAccessLevel].
 */
@Serializable
data class RbacFieldRuleRequest(
    val fieldName: String,
    val accessLevel: RbacAccessLevel
)
