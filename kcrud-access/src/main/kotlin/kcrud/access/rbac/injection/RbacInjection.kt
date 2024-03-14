/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.access.rbac.injection

import kcrud.access.actor.repository.IActorRepository
import kcrud.access.rbac.repository.field_rule.IRbacFieldRuleRepository
import kcrud.access.rbac.repository.field_rule.RbacFieldRuleRepository
import kcrud.access.rbac.repository.resource_rule.IRbacResourceRuleRepository
import kcrud.access.rbac.repository.resource_rule.RbacResourceRuleRepository
import kcrud.access.rbac.repository.role.IRbacRoleRepository
import kcrud.access.rbac.repository.role.RbacRoleRepository
import kcrud.access.rbac.service.RbacService
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * RBAC dependency injection module.
 */
object RbacInjection {

    fun get(): Module {
        return module {
            single<IRbacFieldRuleRepository> {
                RbacFieldRuleRepository()
            }

            single<IRbacResourceRuleRepository> {
                RbacResourceRuleRepository(fieldRuleRepository = get<IRbacFieldRuleRepository>())
            }

            single<IRbacRoleRepository> {
                RbacRoleRepository(resourceRuleRepository = get<IRbacResourceRuleRepository>())
            }

            single<RbacService> {
                RbacService(
                    actorRepository = get<IActorRepository>(),
                    roleRepository = get<IRbacRoleRepository>(),
                    resourceRuleRepository = get<IRbacResourceRuleRepository>()
                )
            }
        }
    }
}
