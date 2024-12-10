/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package krud.access.domain.rbac.model.field

import kotlinx.serialization.Serializable
import krud.database.schema.admin.rbac.type.RbacAccessLevel

/**
 * Request to create a RBAC field rule.
 * Field rules are never updated, only re-created and deleted.
 * So, when updating a scope rule, all field rules are deleted and re-created.
 *
 * @property fieldName The name of the field being targeted.
 * @property accessLevel The field [RbacAccessLevel].
 */
@Serializable
public data class RbacFieldRuleRequest internal constructor(
    val fieldName: String,
    val accessLevel: RbacAccessLevel
)
