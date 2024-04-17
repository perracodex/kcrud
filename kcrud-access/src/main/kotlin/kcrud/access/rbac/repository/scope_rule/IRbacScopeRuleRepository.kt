/*
 * Copyright (c) 2023-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.access.rbac.repository.scope_rule

import kcrud.access.rbac.entity.scope_rule.RbacScopeRuleEntity
import kcrud.access.rbac.entity.scope_rule.RbacScopeRuleRequest
import java.util.*

/**
 * Repository for [RbacScopeRuleEntity] data.
 *
 * @see RbacScopeRuleRequest
 */
interface IRbacScopeRuleRepository {

    /**
     * Updates an existing role with the given set of [RbacScopeRuleRequest] entries.
     *
     * All the existing scope rules for the given [roleId] will be replaced by the new ones.
     *
     * @param roleId The id of the role for which the rules are updated.
     * @param scopeRuleRequests The new set of [RbacScopeRuleRequest] entries to set.
     * @return The new number of rows.
     */
    fun replace(roleId: UUID, scopeRuleRequests: List<RbacScopeRuleRequest>?): Int
}
