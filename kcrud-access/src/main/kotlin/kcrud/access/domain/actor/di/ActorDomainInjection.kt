/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.domain.actor.di

import kcrud.access.credential.CredentialService
import kcrud.access.domain.actor.repository.ActorRepository
import kcrud.access.domain.actor.repository.IActorRepository
import kcrud.access.domain.actor.service.ActorService
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Actor dependency injection module.
 */
public object ActorDomainInjection {

    /**
     * Get the dependency injection module for the Actor domain.
     */
    public fun get(): Module {
        return module {
            singleOf(::ActorRepository) {
                bind<IActorRepository>()
                createdAtStart()
            }

            singleOf(::CredentialService) {
                createdAtStart()
            }

            singleOf(::ActorService) {
                createdAtStart()
            }
        }
    }
}
