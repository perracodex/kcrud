/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employment.di

import kcrud.base.env.SessionContext
import kcrud.domain.employment.repository.EmploymentRepository
import kcrud.domain.employment.repository.IEmploymentRepository
import kcrud.domain.employment.service.EmploymentService
import org.koin.core.module.Module
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
                scoped<IEmploymentRepository> {
                    EmploymentRepository(sessionContext = get<SessionContext>())
                }

                scoped<EmploymentService> { parameters ->
                    EmploymentService(
                        sessionContext = parameters.get<SessionContext>(),
                        employmentRepository = get<IEmploymentRepository>()
                    )
                }
            }

            // Definitions for non-scoped (global) access.

            factory<IEmploymentRepository> {
                EmploymentRepository(sessionContext = get<SessionContext>())
            }

            factory<EmploymentService> { parameters ->
                EmploymentService(
                    sessionContext = parameters.get<SessionContext>(),
                    employmentRepository = get<IEmploymentRepository>()
                )
            }
        }
    }
}
