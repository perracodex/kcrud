/*
 * Copyright (c) 2024-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

import io.ktor.test.dispatcher.*
import kcrud.access.actor.di.ActorInjection
import kcrud.access.actor.entity.ActorEntity
import kcrud.access.actor.entity.ActorRequest
import kcrud.access.actor.service.ActorService
import kcrud.access.rbac.di.RbacInjection
import kcrud.access.rbac.entity.role.RbacRoleEntity
import kcrud.access.rbac.entity.role.RbacRoleRequest
import kcrud.access.rbac.entity.scope_rule.RbacScopeRuleRequest
import kcrud.access.rbac.service.RbacService
import kcrud.base.database.schema.admin.rbac.types.RbacAccessLevel
import kcrud.base.database.schema.admin.rbac.types.RbacScope
import kcrud.base.utils.TestUtils
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*
import kotlin.test.*

/**
 * Test for the [RbacService].
 * Not for RBAC permission checks, but instead for the service interface.
 * For an example of RBAC permission checks, see the RBAC tests in the application service.
 */
class RbacServiceTest : KoinComponent {

    @BeforeTest
    fun setUp() {
        TestUtils.loadSettings()
        TestUtils.setupDatabase()
        TestUtils.setupKoin(modules = listOf(RbacInjection.get(), ActorInjection.get()))
    }

    @AfterTest
    fun tearDown() {
        TestUtils.tearDown()
    }

    @Test
    fun testRoleCreation(): Unit = testSuspend {
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
        val roleEntity: RbacRoleEntity = rbacService.createRole(roleRequest = roleRequest)
        val existingRoleEntity: RbacRoleEntity? = rbacService.findRoleById(roleId = roleEntity.id)
        assertNotNull(actual = existingRoleEntity, message = "The role was not found in the database after it was created.")
        assertEquals(expected = roleName, actual = existingRoleEntity.roleName)
        assertEquals(expected = description, actual = existingRoleEntity.description)

        // Try to create the same role again.
        assertFailsWith<ExposedSQLException> {
            rbacService.createRole(roleRequest = roleRequest)
        }

        // Update the role.
        val newRoleName = "new_role_name"
        val newDescription = "New role description"
        val updatedRoleEntity: RbacRoleEntity? = rbacService.updateRole(
            roleId = roleEntity.id,
            roleRequest = roleRequest.copy(
                roleName = newRoleName,
                description = newDescription
            )
        )
        assertNotNull(actual = updatedRoleEntity, message = "The role was not updated.")
        assertEquals(expected = newRoleName, actual = updatedRoleEntity.roleName)
        assertEquals(expected = newDescription, actual = updatedRoleEntity.description)
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
        val roleEntity: RbacRoleEntity = rbacService.createRole(roleRequest = roleRequest)

        // Create the actor.
        val actorService: ActorService by inject()

        val username = "any_username"
        val password = "any_password"
        val actorId: UUID = actorService.create(
            actorRequest = ActorRequest(
                roleId = roleEntity.id,
                username = username,
                password = password,
                isLocked = false
            )
        )

        var actor: ActorEntity? = actorService.findByUsername(username = username)
        assertNotNull(actual = actor, message = "The user was not found in the database after it was created.")
        assertEquals(expected = username, actual = actor.username)
        assertEquals(expected = password, actual = actor.password)

        actor = actorService.findById(actorId = actorId)
        assertNotNull(actual = actor, message = "The user was not found in the database after it was created.")
        assertEquals(expected = username, actual = actor.username)
        assertEquals(expected = password, actual = actor.password)

        // Try to create the same actor again.
        // This should fail because the username is unique.
        assertFailsWith<ExposedSQLException> {
            actorService.create(
                actorRequest = ActorRequest(
                    roleId = roleEntity.id,
                    username = username,
                    password = password,
                    isLocked = false
                )
            )
        }

        // Find a role by Actor ID.
        val roleByActor: RbacRoleEntity? = rbacService.findRoleByActorId(actorId = actorId)
        assertNotNull(actual = roleByActor, message = "The role was not found in the database after it was created.")
        assertEquals(expected = roleName, actual = roleByActor.roleName)
        assertEquals(expected = description, actual = roleByActor.description)
    }
}
