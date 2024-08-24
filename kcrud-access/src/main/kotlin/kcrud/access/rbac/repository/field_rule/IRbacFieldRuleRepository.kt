/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.rbac.repository.field_rule

import kcrud.access.rbac.entity.field_rule.RbacFieldRuleEntity
import kcrud.access.rbac.entity.field_rule.RbacFieldRuleRequest
import kcrud.access.rbac.entity.scope_rule.RbacScopeRuleRequest
import kotlin.uuid.Uuid

/**
 * Repository for [RbacFieldRuleEntity] data.
 *
 * @see RbacFieldRuleRequest
 */
interface IRbacFieldRuleRepository {

    /**
     * Updates an existing scope rule with the given set of [RbacFieldRuleRequest] entries.
     *
     * All the existing field rules for the concrete scope rule will be replaced by the new ones.
     *
     * @param scopeRuleId The target [RbacScopeRuleRequest] being updated.
     * @param requestList The new set of [RbacFieldRuleRequest] entries to set.
     * @return The new number of rows.
     */
    fun replace(scopeRuleId: Uuid, requestList: List<RbacFieldRuleRequest>?): Int
}
