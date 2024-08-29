/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.rbac.entity.field

import kcrud.base.database.schema.admin.rbac.types.RbacAccessLevel
import kotlinx.serialization.Serializable

/**
 * Entity to create a RBAC field rule.
 * Field rules are never updated, only re-created and deleted.
 * So, when updating a scope rule, all field rules are deleted and re-created.
 *
 * @property fieldName The name of the field being targeted.
 * @property accessLevel The field [RbacAccessLevel].
 */
@Serializable
public data class RbacFieldRuleRequest(
    val fieldName: String,
    val accessLevel: RbacAccessLevel
)
