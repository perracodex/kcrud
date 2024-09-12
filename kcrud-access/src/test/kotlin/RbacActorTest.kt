/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

import io.ktor.test.dispatcher.*
import kcrud.access.actor.di.ActorDomainInjection
import kcrud.access.actor.model.ActorDto
import kcrud.access.actor.model.ActorRequest
import kcrud.access.actor.service.ActorService
import kcrud.access.rbac.di.RbacDomainInjection
import kcrud.access.rbac.model.role.RbacRoleDto
import kcrud.access.rbac.model.role.RbacRoleRequest
import kcrud.access.rbac.model.scope.RbacScopeRuleRequest
import kcrud.access.rbac.service.RbacService
import kcrud.base.database.schema.admin.rbac.types.RbacAccessLevel
import kcrud.base.database.schema.admin.rbac.types.RbacScope
import kcrud.base.utils.TestUtils
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.test.*
import kotlin.uuid.Uuid

/**
 * Test for the [RbacService].
 * Not for RBAC permission checks, but instead for the service interface.
 * For an example of RBAC permission checks, see the RBAC tests in the application service.
 */
class RbacActorTest : KoinComponent {

    @BeforeTest
    fun setUp() {
        TestUtils.loadSettings()
        TestUtils.setupDatabase()
        TestUtils.setupKoin(modules = listOf(RbacDomainInjection.get(), ActorDomainInjection.get()))
    }

    @AfterTest
    fun tearDown() {
        TestUtils.tearDown()
    }

    @Test
    fun testActor(): Unit = testSuspend {
        val roleName = "any_role_name"
        val description = "Any role description"

        val roleRequest = RbacRoleRequest(
            roleName = roleName,
            description = description,
            isSuper = false,
            scopeRules = listOf(
                RbacScopeRuleRequest(
                    scope = RbacScope.SYSTEM,
                    accessLevel = RbacAccessLevel.FULL
                )
            )
        )

        val rbacService: RbacService by inject()

        // Create the role.
        val rbacRole: RbacRoleDto = rbacService.createRole(roleRequest = roleRequest)

        // Create the actor.
        val actorService: ActorService by inject()

        val username = "any_username"
        val password = "any_password"
        val actorId: Uuid = actorService.create(
            actorRequest = ActorRequest(
                roleId = rbacRole.id,
                username = username,
                password = password,
                isLocked = false
            )
        )

        var actor: ActorDto? = actorService.findByUsername(username = username)
        assertNotNull(actual = actor, message = "The actor was not found in the database after it was created.")
        assertEquals(expected = username, actual = actor.username)
        assertEquals(expected = password, actual = actor.password)

        actor = actorService.findById(actorId = actorId)
        assertNotNull(actual = actor, message = "The actor was not found in the database after it was created.")
        assertEquals(expected = username, actual = actor.username)
        assertEquals(expected = password, actual = actor.password)

        // Try to create the same actor again.
        // This should fail because the username is unique.
        assertFailsWith<ExposedSQLException> {
            actorService.create(
                actorRequest = ActorRequest(
                    roleId = rbacRole.id,
                    username = username,
                    password = password,
                    isLocked = false
                )
            )
        }

        // Find a role by Actor ID.
        val roleByActor: RbacRoleDto? = rbacService.findRoleByActorId(actorId = actorId)
        assertNotNull(actual = roleByActor, message = "The role was not found in the database after it was created.")
        assertEquals(expected = roleName, actual = roleByActor.roleName)
        assertEquals(expected = description, actual = roleByActor.description)
    }
}
