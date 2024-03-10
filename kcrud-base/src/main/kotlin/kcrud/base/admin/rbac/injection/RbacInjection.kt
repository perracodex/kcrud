/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.admin.rbac.injection

import kcrud.base.admin.actor.repository.IActorRepository
import kcrud.base.admin.rbac.repository.field_rule.IRbacFieldRuleRepository
import kcrud.base.admin.rbac.repository.field_rule.RbacFieldRuleRepository
import kcrud.base.admin.rbac.repository.resource_rule.IRbacResourceRuleRepository
import kcrud.base.admin.rbac.repository.resource_rule.RbacResourceRuleRepository
import kcrud.base.admin.rbac.repository.role.IRbacRoleRepository
import kcrud.base.admin.rbac.repository.role.RbacRoleRepository
import kcrud.base.admin.rbac.service.RbacService
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
