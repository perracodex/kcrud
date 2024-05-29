/*
 * Copyright (c) 2023-Present Perracodex. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.server.plugins

import io.ktor.server.application.*
import kcrud.access.actor.di.ActorInjection
import kcrud.access.rbac.di.RbacInjection
import kcrud.domain.employee.di.EmployeeInjection
import kcrud.domain.employment.di.EmploymentInjection
import org.koin.ktor.plugin.Koin

/**
 * Sets up and initializes dependency injection using the Koin framework.
 *
 * See: [Koin for Ktor Documentation](https://insert-koin.io/docs/quickstart/ktor)
 */
fun Application.configureKoin() {

    install(plugin = Koin) {
        modules(
            RbacInjection.get(),
            ActorInjection.get(),
            EmployeeInjection.get(),
            EmploymentInjection.get()
        )
    }
}
