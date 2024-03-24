/*
 * Copyright (c) 2024-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.access.actor.di

import kcrud.access.actor.repository.ActorRepository
import kcrud.access.actor.repository.IActorRepository
import kcrud.access.actor.service.ActorService
import kcrud.access.credential.CredentialService
import kcrud.access.rbac.repository.role.IRbacRoleRepository
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
