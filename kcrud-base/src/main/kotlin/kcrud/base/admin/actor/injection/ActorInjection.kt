/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.admin.actor.injection

import kcrud.base.admin.actor.repository.ActorRepository
import kcrud.base.admin.actor.repository.IActorRepository
import kcrud.base.admin.actor.service.ActorService
import kcrud.base.admin.rbac.repository.role.IRbacRoleRepository
import kcrud.base.security.service.CredentialService
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Actor dependency injection module.
 */
object ActorInjection {

    fun get(): Module {
        return module {
            single<IActorRepository>(createdAtStart = true) {
                ActorRepository(roleRepository = get<IRbacRoleRepository>())
            }

            single<CredentialService>(createdAtStart = true) {
                CredentialService()
            }

            single<ActorService>(createdAtStart = true) {
                ActorService(
                    roleRepository = get<IRbacRoleRepository>(),
                    actorRepository = get<IActorRepository>()
                )
            }
        }
    }
}
