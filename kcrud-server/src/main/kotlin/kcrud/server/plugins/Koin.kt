/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.server.plugins

import io.ktor.server.application.*
import kcrud.access.actor.di.ActorDomainInjection
import kcrud.access.rbac.di.RbacDomainInjection
import kcrud.domain.employee.di.EmployeeDomainInjection
import kcrud.domain.employment.di.EmploymentDomainInjection
import org.koin.ktor.plugin.Koin

/**
 * Sets up and initializes dependency injection using the Koin framework,
 *
 * See: [Koin for Ktor Documentation](https://insert-koin.io/docs/quickstart/ktor)
 */
internal fun Application.configureKoin() {

    install(plugin = Koin) {
        // Load all the DI modules for the application.
        modules(
            RbacDomainInjection.get(),
            ActorDomainInjection.get(),
            EmployeeDomainInjection.get(),
            EmploymentDomainInjection.get()
        )
    }
}
