/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employment.di

import kcrud.domain.employment.repository.EmploymentRepository
import kcrud.domain.employment.repository.IEmploymentRepository
import kcrud.domain.employment.service.EmploymentService
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.scopedOf
import org.koin.dsl.module
import org.koin.ktor.plugin.RequestScope

/**
 * Employment domain dependency injection module.
 */
public object EmploymentDomainInjection {

    /**
     * Get the dependency injection module for the Employment domain.
     */
    public fun get(): Module {
        return module {
            // Scoped definitions within RequestScope for single request lifecycle.
            // Services receive the session context as a parameter. Repositories,
            // which should only be accessed by services, do not receive it directly.

            scope<RequestScope> {
                scopedOf(::EmploymentRepository) {
                    bind<IEmploymentRepository>()
                }

                scopedOf(::EmploymentService)
            }

            // Definitions for non-scoped (global) access.

            factoryOf(::EmploymentRepository) {
                bind<IEmploymentRepository>()
            }

            factoryOf(::EmploymentService)
        }
    }
}
