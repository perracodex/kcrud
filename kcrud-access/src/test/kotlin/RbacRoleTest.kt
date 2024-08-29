/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

import io.ktor.test.dispatcher.*
import kcrud.access.actor.di.ActorDomainInjection
import kcrud.access.rbac.di.RbacDomainInjection
import kcrud.access.rbac.entity.role.RbacRoleEntity
import kcrud.access.rbac.entity.role.RbacRoleRequest
import kcrud.access.rbac.entity.scope.RbacScopeRuleRequest
import kcrud.access.rbac.service.RbacService
import kcrud.base.database.schema.admin.rbac.types.RbacAccessLevel
import kcrud.base.database.schema.admin.rbac.types.RbacScope
import kcrud.base.utils.TestUtils
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.test.*

/**
 * Test for the [RbacService].
 * Not for RBAC permission checks, but instead for the service interface.
 * For an example of RBAC permission checks, see the RBAC tests in the application service.
 */
class RbacRoleTest : KoinComponent {

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
}
