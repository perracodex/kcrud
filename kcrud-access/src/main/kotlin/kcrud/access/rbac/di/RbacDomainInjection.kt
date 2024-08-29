/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.rbac.di

import kcrud.access.actor.repository.IActorRepository
import kcrud.access.rbac.repository.field.IRbacFieldRuleRepository
import kcrud.access.rbac.repository.field.RbacFieldRuleRepository
import kcrud.access.rbac.repository.role.IRbacRoleRepository
import kcrud.access.rbac.repository.role.RbacRoleRepository
import kcrud.access.rbac.repository.scope.IRbacScopeRuleRepository
import kcrud.access.rbac.repository.scope.RbacScopeRuleRepository
import kcrud.access.rbac.service.RbacService
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * RBAC dependency injection module.
 */
public object RbacDomainInjection {

    /**
     * Get the dependency injection module for the RBAC domain.
     */
    public fun get(): Module {
        return module {
            single<IRbacFieldRuleRepository> {
                RbacFieldRuleRepository()
            }

            single<IRbacScopeRuleRepository> {
                RbacScopeRuleRepository(fieldRuleRepository = get<IRbacFieldRuleRepository>())
            }

            single<IRbacRoleRepository> {
                RbacRoleRepository(scopeRuleRepository = get<IRbacScopeRuleRepository>())
            }

            single<RbacService> {
                RbacService(
                    actorRepository = get<IActorRepository>(),
                    roleRepository = get<IRbacRoleRepository>(),
                    scopeRuleRepository = get<IRbacScopeRuleRepository>()
                )
            }
        }
    }
}
