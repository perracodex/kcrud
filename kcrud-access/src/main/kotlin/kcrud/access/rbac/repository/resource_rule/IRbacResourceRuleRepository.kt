/*
 * Copyright (c) 2023-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.access.rbac.repository.resource_rule

import kcrud.access.rbac.entity.resource_rule.RbacResourceRuleEntity
import kcrud.access.rbac.entity.resource_rule.RbacResourceRuleRequest
import java.util.*

/**
 * Repository for [RbacResourceRuleEntity] data.
 *
 * @see RbacResourceRuleRequest
 */
interface IRbacResourceRuleRepository {

    /**
     * Updates an existing role with the given set of [RbacResourceRuleRequest] entries.
     *
     * All the existing resource rules for the given [roleId] will be replaced by the new ones.
     *
     * @param roleId The id of the role for which the rules are updated.
     * @param resourceRuleRequests The new set of [RbacResourceRuleRequest] entries to set.
     * @return The new number of rows.
     */
    fun replace(roleId: UUID, resourceRuleRequests: List<RbacResourceRuleRequest>?): Int
}
