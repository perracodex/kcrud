/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employee.di

import kcrud.base.env.SessionContext
import kcrud.domain.contact.repository.ContactRepository
import kcrud.domain.contact.repository.IContactRepository
import kcrud.domain.employee.repository.EmployeeRepository
import kcrud.domain.employee.repository.IEmployeeRepository
import kcrud.domain.employee.service.EmployeeService
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.ktor.plugin.RequestScope

/**
 * Employee domain dependency injection module.
 */
object EmployeeDomainInjection {

    /**
     * Get the dependency injection module for the Employee domain.
     */
    fun get(): Module {
        return module {
            // Definitions for scoped access within RequestScope.
            // These are the definitions that are scoped to the lifecycle of a single request.
            scope<RequestScope> {
                scoped<IContactRepository> {
                    ContactRepository(sessionContext = get<SessionContext>())
                }

                scoped<IEmployeeRepository> {
                    EmployeeRepository(
                        sessionContext = get<SessionContext>(),
                        contactRepository = get<IContactRepository>()
                    )
                }

                scoped<EmployeeService> { parameters ->
                    EmployeeService(
                        sessionContext = parameters.get<SessionContext>(),
                        employeeRepository = get<IEmployeeRepository>()
                    )
                }
            }

            // Definitions for non-scoped (global) access.
            single<IContactRepository> {
                ContactRepository(sessionContext = get<SessionContext>())
            }

            single<IEmployeeRepository> {
                EmployeeRepository(
                    sessionContext = get<SessionContext>(),
                    contactRepository = get<IContactRepository>()
                )
            }

            single<EmployeeService> {
                EmployeeService(
                    sessionContext = get<SessionContext>(),
                    employeeRepository = get<IEmployeeRepository>()
                )
            }
        }
    }
}