/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.rbac.model.scope

import kcrud.access.rbac.model.field.RbacFieldRuleRequest
import kcrud.core.database.schema.admin.rbac.type.RbacAccessLevel
import kcrud.core.database.schema.admin.rbac.type.RbacScope
import kotlinx.serialization.Serializable

/**
 * Request to create a RBAC scope rule.
 * Scope rules are never updated, only re-created and deleted.
 * Therefore, when updating an RBAC role, all its associated scope rules are deleted and re-created.
 *
 * A scope can be any concept: a database table, a REST endpoint, a UI element, etc.
 * Is up to the designer to define what a scope is, and act accordingly when its
 * associated RBAC rule is verified.
 *
 * @property scope The [RbacScope] being targeted.
 * @property accessLevel The required [RbacAccessLevel] for the [RbacScope].
 * @property fieldRules The list of [RbacFieldRuleRequest] for the scope. Or null if there are no field rules.
 */
@Serializable
public data class RbacScopeRuleRequest(
    val scope: RbacScope,
    val accessLevel: RbacAccessLevel,
    val fieldRules: List<RbacFieldRuleRequest>? = null
)
