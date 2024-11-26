/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

import io.ktor.test.dispatcher.*
import kcrud.access.domain.actor.di.ActorDomainInjection
import kcrud.access.domain.rbac.di.RbacDomainInjection
import kcrud.access.domain.rbac.model.role.RbacRole
import kcrud.access.domain.rbac.model.role.RbacRoleRequest
import kcrud.access.domain.rbac.model.scope.RbacScopeRuleRequest
import kcrud.access.domain.rbac.service.RbacService
import kcrud.core.test.TestUtils
import kcrud.database.schema.admin.rbac.type.RbacAccessLevel
import kcrud.database.schema.admin.rbac.type.RbacScope
import kcrud.database.test.DatabaseTestUtils
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
        DatabaseTestUtils.setupDatabase()
        TestUtils.setupKoin(
            modules = listOf(
                RbacDomainInjection.get(),
                ActorDomainInjection.get()
            )
        )
    }

    @AfterTest
    fun tearDown() {
        DatabaseTestUtils.closeDatabase()
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
        val rbacRole: RbacRole = rbacService.createRole(roleRequest = roleRequest)
        val existingRole: RbacRole? = rbacService.findRoleById(roleId = rbacRole.id)
        assertNotNull(actual = existingRole, message = "The role was not found in the database after it was created.")
        assertEquals(expected = roleName, actual = existingRole.roleName)
        assertEquals(expected = description, actual = existingRole.description)

        // Try to create the same role again.
        assertFailsWith<ExposedSQLException> {
            rbacService.createRole(roleRequest = roleRequest)
        }

        // Update the role.
        val newRoleName = "new_role_name"
        val newDescription = "New role description"
        val updatedRole: RbacRole? = rbacService.updateRole(
            roleId = rbacRole.id,
            roleRequest = roleRequest.copy(
                roleName = newRoleName,
                description = newDescription
            )
        )
        assertNotNull(actual = updatedRole, message = "The role was not updated.")
        assertEquals(expected = newRoleName, actual = updatedRole.roleName)
        assertEquals(expected = newDescription, actual = updatedRole.description)
    }
}
