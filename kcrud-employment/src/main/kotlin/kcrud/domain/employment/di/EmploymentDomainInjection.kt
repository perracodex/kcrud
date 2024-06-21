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
object EmploymentDomainInjection {

    /**
     * Get the dependency injection module for the Employment domain.
     */
    fun get(): Module {
        return module {
            // Definitions for scoped access within RequestScope.
            // These are the definitions that are scoped to the lifecycle of a single request.
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
            single<IEmploymentRepository> {
                EmploymentRepository(sessionContext = get<SessionContext>())
            }

            single<EmploymentService> {
                EmploymentService(
                    sessionContext = get<SessionContext>(),
                    employmentRepository = get<IEmploymentRepository>()
                )
            }
        }
    }
}
