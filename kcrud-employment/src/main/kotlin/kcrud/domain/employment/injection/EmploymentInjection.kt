/*
 * Copyright (c) 2024-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.domain.employment.injection

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
object EmploymentInjection {

    fun get(): Module {
        return module {
            // Definitions for scoped access within RequestScope.
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
