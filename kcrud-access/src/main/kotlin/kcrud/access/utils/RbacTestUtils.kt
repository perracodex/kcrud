/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.utils

import kcrud.access.actor.model.ActorDto
import kcrud.access.actor.model.ActorRequest
import kcrud.access.actor.service.ActorService
import kcrud.access.actor.service.DefaultActorFactory
import kcrud.access.rbac.model.role.RbacRoleDto
import kcrud.access.rbac.model.role.RbacRoleRequest
import kcrud.access.rbac.model.scope.RbacScopeRuleRequest
import kcrud.access.rbac.service.RbacService
import kcrud.access.token.annotation.TokenAPI
import kcrud.access.token.service.AuthenticationTokenService
import kcrud.base.database.schema.admin.rbac.types.RbacAccessLevel
import kcrud.base.database.schema.admin.rbac.types.RbacScope
import kcrud.base.env.SessionContext
import org.koin.java.KoinJavaComponent.getKoin
import kotlin.test.assertNotNull
import kotlin.uuid.Uuid

/**
 * Common utilities for RBAC unit testing.
 */
public object RbacTestUtils {

    /**
     * Creates a new authentication token for the default admin actor.
     *
     * @return The authentication token.
     */
    @OptIn(TokenAPI::class)
    public suspend fun newAuthenticationToken(): String {
        val actorService: ActorService = getKoin().get()
        val username: String = DefaultActorFactory.RoleName.ADMIN.name.lowercase()
        val actor: ActorDto? = actorService.findByUsername(username = username)
        assertNotNull(actual = actor)

        val sessionContext = SessionContext(
            actorId = actor.id,
            username = actor.username,
            roleId = actor.role.id,
            schema = null,
        )

        return AuthenticationTokenService.generate(sessionContext = sessionContext)
    }

    /**
     * Creates a new authentication token for the given access level and test iteration.
     *
     * @param accessLevel The access level.
     * @param testIteration The test iteration.
     * @return The authentication token.
     */
    @OptIn(TokenAPI::class)
    public suspend fun newAuthenticationToken(accessLevel: RbacAccessLevel, testIteration: Int): String {
        val actor: ActorDto = createActor(accessLevel = accessLevel, iteration = testIteration)

        val sessionContext = SessionContext(
            actorId = actor.id,
            username = actor.username,
            roleId = actor.role.id,
            schema = null
        )

        return AuthenticationTokenService.generate(sessionContext = sessionContext)
    }

    /**
     * Creates a new actor for the given access level and test iteration.
     *
     * @param accessLevel The access level.
     * @param iteration The test iteration.
     * @return The created actor.
     */
    private suspend fun createActor(accessLevel: RbacAccessLevel, iteration: Int): ActorDto {
        // Setup actor and role for the test.
        val scopeRuleRequest = RbacScopeRuleRequest(
            scope = RbacScope.EMPLOYEE_RECORDS,
            accessLevel = accessLevel,
            fieldRules = null
        )
        val roleRequest = RbacRoleRequest(
            roleName = "${accessLevel.name}_$iteration".lowercase(), // Unique role name per iteration
            description = "Role for ${accessLevel.name} access, iteration $iteration",
            isSuper = false,
            scopeRules = listOf(scopeRuleRequest)
        )

        val rbacService: RbacService = getKoin().get()
        val role: RbacRoleDto = rbacService.createRole(roleRequest = roleRequest)
        assertNotNull(actual = role, message = "Role should not be null")

        // Create the Actor with the associated role.
        val username: String = "actor_${accessLevel.name}_$iteration".lowercase() // Unique username per iteration.
        val password: String = "password_$iteration".lowercase() // Unique password per iteration.
        val actorRequest = ActorRequest(
            roleId = role.id,
            username = username,
            password = password,
            isLocked = false
        )

        val actorService: ActorService = getKoin().get()
        val actorId: Uuid = actorService.create(actorRequest = actorRequest)
        assertNotNull(actual = actorId, message = "Actor ID should not be null")

        // Retrieve the Actor.
        val actor: ActorDto? = actorService.findById(actorId = actorId)
        assertNotNull(actual = actor, message = "Actor should not be null")

        return actor
    }
}
