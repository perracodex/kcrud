/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.domain.rbac.di

import kcrud.access.domain.rbac.repository.field.IRbacFieldRuleRepository
import kcrud.access.domain.rbac.repository.field.RbacFieldRuleRepository
import kcrud.access.domain.rbac.repository.role.IRbacRoleRepository
import kcrud.access.domain.rbac.repository.role.RbacRoleRepository
import kcrud.access.domain.rbac.repository.scope.IRbacScopeRuleRepository
import kcrud.access.domain.rbac.repository.scope.RbacScopeRuleRepository
import kcrud.access.domain.rbac.service.RbacService
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
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
            singleOf(::RbacFieldRuleRepository) {
                bind<IRbacFieldRuleRepository>()
            }

            singleOf(::RbacScopeRuleRepository) {
                bind<IRbacScopeRuleRepository>()
            }

            singleOf(::RbacRoleRepository) {
                bind<IRbacRoleRepository>()
            }

            singleOf(::RbacService)
        }
    }
}
