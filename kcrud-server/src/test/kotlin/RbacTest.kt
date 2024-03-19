/*
 * Copyright (c) 2024-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.test.dispatcher.*
import io.mockk.every
import io.mockk.mockk
import kcrud.access.actor.entity.ActorEntity
import kcrud.access.actor.entity.ActorRequest
import kcrud.access.actor.service.ActorService
import kcrud.access.rbac.entity.resource_rule.RbacResourceRuleRequest
import kcrud.access.rbac.entity.role.RbacRoleEntity
import kcrud.access.rbac.entity.role.RbacRoleRequest
import kcrud.access.rbac.service.RbacService
import kcrud.access.token.AuthenticationTokenService
import kcrud.base.database.schema.admin.rbac.types.RbacAccessLevel
import kcrud.base.database.schema.admin.rbac.types.RbacResource
import kcrud.base.database.schema.employee.types.Honorific
import kcrud.base.database.schema.employee.types.MaritalStatus
import kcrud.base.env.SessionContext
import kcrud.base.utils.KLocalDate
import kcrud.base.utils.TestUtils
import kcrud.domain.contact.entity.ContactRequest
import kcrud.domain.employee.entity.EmployeeEntity
import kcrud.domain.employee.entity.EmployeeRequest
import kcrud.domain.employee.service.EmployeeService
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*
import kotlin.test.*

class RbacTest : KoinComponent {

    @BeforeTest
    fun setUp() {
        TestUtils.loadSettings()
        TestUtils.setupDatabase()
    }

    @AfterTest
    fun tearDown() {
        TestUtils.tearDown()
    }

    /**
     * Tests Role-Based Access Control (RBAC) enforcement by systematically tests different combinations
     * of access levels against various API operations.
     * Verifies that each API endpoint honors the correct access level by either permitting or denying requests.
     */
    @Test
    fun testAccess() = testApplication {
        startApplication()

        // Prepare a generic employee request used in POST and PUT endpoints.
        val employeeRequest = EmployeeRequest(
            firstName = "AnyName",
            lastName = "AnySurname",
            dob = KLocalDate(year = 2000, monthNumber = 1, dayOfMonth = 1),
            honorific = Honorific.MR,
            maritalStatus = MaritalStatus.MARRIED,
            contact = ContactRequest(
                email = "AnyName.AnySurname@email.com",
                phone = "+34-611-222-333"
            )
        )
        val employeeRequestJson: String = Json.encodeToString(
            serializer = EmployeeRequest.serializer(), value = employeeRequest
        )

        // List defining the expected outcomes for various operations based on different RBAC levels.
        // Placeholder {employee_id} will be replaced with an actual employee's ID during tests.
        // 'first' represents the API endpoint for creating an employee.
        // 'second' is the HTTP method used for this action.
        // 'third' is a map of RBACAccessLevel to the expected HttpStatusCode for that level of access.
        val endpointsAndExpectedOutcomes = listOf(
            // Creating an employee.
            Triple(
                first = "/v1/employees",
                second = "POST",
                third = mapOf(
                    RbacAccessLevel.NONE to HttpStatusCode.Forbidden, // No access should result in Forbidden.
                    RbacAccessLevel.VIEW to HttpStatusCode.Forbidden, // View-only access should still forbid creation.
                    RbacAccessLevel.FULL to HttpStatusCode.Created // Full access allows creating, should return Created status.
                )
            ),
            // Getting all employees.
            Triple(
                first = "/v1/employees",
                second = "GET",
                third = mapOf(
                    RbacAccessLevel.NONE to HttpStatusCode.Forbidden, // No access results in Forbidden status.
                    RbacAccessLevel.VIEW to HttpStatusCode.OK, // View access should successfully retrieve the list.
                    RbacAccessLevel.FULL to HttpStatusCode.OK // Full access should also successfully retrieve the list.
                )
            ),
            // Getting a single employee by ID:
            Triple(
                first = "/v1/employees/{employee_id}",
                second = "GET",
                third = mapOf(
                    RbacAccessLevel.NONE to HttpStatusCode.Forbidden, // No access results in Forbidden.
                    RbacAccessLevel.VIEW to HttpStatusCode.OK, // View access allows retrieving individual details.
                    RbacAccessLevel.FULL to HttpStatusCode.OK // Full access allows for the same.
                )
            ),
            // Updating an employee by ID.
            Triple(
                first = "/v1/employees/{employee_id}",
                second = "PUT",
                third = mapOf(
                    RbacAccessLevel.NONE to HttpStatusCode.Forbidden, // No access should result in Forbidden.
                    RbacAccessLevel.VIEW to HttpStatusCode.Forbidden, // View access does not permit updating.
                    RbacAccessLevel.FULL to HttpStatusCode.OK // Full access should allow updating.
                )
            ),
            // Deleting an employee by ID.
            Triple(
                first = "/v1/employees/{employee_id}",
                second = "DELETE",
                third = mapOf(
                    RbacAccessLevel.NONE to HttpStatusCode.Forbidden, // No access should result in Forbidden.
                    RbacAccessLevel.VIEW to HttpStatusCode.Forbidden, // View access does not permit deletion.
                    RbacAccessLevel.FULL to HttpStatusCode.OK // Full access should allow deletion.
                )
            ),
            // Deleting all employees.
            Triple(
                first = "/v1/employees",
                second = "DELETE",
                third = mapOf(
                    RbacAccessLevel.NONE to HttpStatusCode.Forbidden, // No access should result in Forbidden.
                    RbacAccessLevel.VIEW to HttpStatusCode.Forbidden, // View access does not permit bulk deletion.
                    RbacAccessLevel.FULL to HttpStatusCode.OK // Full access should allow bulk deletion.
                )
            )
        )

        var testIteration = 0

        testSuspend {
            // Create an employee to be used in the test cases for individual operations.
            val createdEmployeeId: UUID = createEmployee().id

            // Iterate over each endpoint and method pair, along with their expected outcomes.
            for ((endpointTemplate, requestMethod, testOutcomes) in endpointsAndExpectedOutcomes) {

                // Update the target endpoint by replacing its placeholder with the actual employee ID.
                // This is necessary for the GET, PUT, and DELETE operations.
                // If the endpoint does not contain the placeholder, it will remain the same.
                val endpoint: String = endpointTemplate.replace(
                    oldValue = "{employee_id}",
                    newValue = createdEmployeeId.toString()
                )

                testOutcomes.forEach { (accessLevel, expectedStatus) ->

                    // Create a unique Actor for each test case to ensure clear RBAC role assignments.
                    val actor: ActorEntity = createActor(accessLevel = accessLevel, iteration = testIteration)
                    testIteration++

                    val sessionContext = SessionContext(
                        actorId = actor.id,
                        username = actor.username,
                        roleId = actor.role.id,
                        schema = null
                    )
                    val authToken: String = AuthenticationTokenService.generate(sessionContext = sessionContext)
                    assertNotNull(actual = authToken, message = "Auth token should not be null")

                    // Make the request call.
                    val response: HttpResponse = when (requestMethod) {
                        "POST" -> client.post(endpoint) {
                            header(key = HttpHeaders.Authorization, value = "Bearer $authToken")
                            contentType(type = ContentType.Application.Json)
                            setBody(body = employeeRequestJson)
                        }

                        "GET" -> client.get(endpoint) {
                            header(key = HttpHeaders.Authorization, value = "Bearer $authToken")
                        }

                        "PUT" -> client.put(endpoint) {
                            header(key = HttpHeaders.Authorization, value = "Bearer $authToken")
                            contentType(type = ContentType.Application.Json)
                            setBody(body = employeeRequestJson)
                        }

                        "DELETE" -> client.delete(endpoint) {
                            header(key = HttpHeaders.Authorization, value = "Bearer $authToken")
                        }

                        else -> throw IllegalArgumentException("Unsupported request method: $requestMethod")
                    }

                    // Assert the response status.
                    assertEquals(expected = expectedStatus, actual = response.status)
                }
            }
        }
    }

    private suspend fun createActor(accessLevel: RbacAccessLevel, iteration: Int): ActorEntity {
        // Setup actor and role for the test.
        val resourceRuleRequest = RbacResourceRuleRequest(
            resource = RbacResource.EMPLOYEE_RECORDS,
            accessLevel = accessLevel,
            fieldRules = null
        )
        val roleRequest = RbacRoleRequest(
            roleName = "${accessLevel.name}_${iteration}".lowercase(), // Unique role name per iteration
            description = "Role for ${accessLevel.name} access, iteration $iteration",
            isSuper = false,
            resourceRules = listOf(resourceRuleRequest)
        )

        val rbacService: RbacService by inject()
        val role: RbacRoleEntity = rbacService.createRole(roleRequest = roleRequest)
        assertNotNull(actual = role, message = "Role should not be null")

        // Create the Actor with the associated role.
        val username = "actor_${accessLevel.name}_${iteration}".lowercase() // Unique username per iteration.
        val password = "pass_${iteration}".lowercase() // Unique password per iteration.
        val actorRequest = ActorRequest(
            roleId = role.id,
            username = username,
            password = password,
            isLocked = false
        )

        val actorService: ActorService by inject()
        val actorId: UUID = actorService.create(actorRequest = actorRequest)
        assertNotNull(actual = actorId, message = "Actor ID should not be null")

        // Retrieve the Actor.
        val actor: ActorEntity? = actorService.findById(actorId = actorId)
        assertNotNull(actual = actor, message = "Actor should not be null")

        return actor
    }

    private suspend fun createEmployee(): EmployeeEntity {
        val firstName = TestUtils.randomName()
        val lastName = TestUtils.randomName()
        val employeeRequest = EmployeeRequest(
            firstName = firstName,
            lastName = lastName,
            dob = TestUtils.randomDob(),
            honorific = Honorific.entries.random(),
            maritalStatus = MaritalStatus.entries.random(),
            contact = ContactRequest(
                email = "$lastName.$firstName@email.com",
                phone = TestUtils.randomPhoneNumber()
            )
        )

        val sessionContext: SessionContext = mockk<SessionContext>()
        every { sessionContext.schema } returns null

        val employeeService: EmployeeService by inject(
            parameters = { org.koin.core.parameter.parametersOf(sessionContext) }
        )

        return employeeService.create(employeeRequest = employeeRequest)
    }
}
