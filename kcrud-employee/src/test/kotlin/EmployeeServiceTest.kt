/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

import io.ktor.test.dispatcher.*
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.perracodex.exposed.pagination.Page
import kcrud.access.actor.di.ActorDomainInjection
import kcrud.access.rbac.di.RbacDomainInjection
import kcrud.core.context.SessionContext
import kcrud.core.database.schema.employee.type.Honorific
import kcrud.core.database.schema.employee.type.MaritalStatus
import kcrud.core.persistence.model.Meta
import kcrud.core.test.TestUtils
import kcrud.core.util.DateTimeUtils.age
import kcrud.domain.contact.model.Contact
import kcrud.domain.employee.di.EmployeeDomainInjection
import kcrud.domain.employee.model.Employee
import kcrud.domain.employee.model.EmployeeRequest
import kcrud.domain.employee.repository.IEmployeeRepository
import kcrud.domain.employee.service.EmployeeService
import kcrud.domain.employee.test.EmployeeTestUtils
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import kotlin.test.*
import kotlin.uuid.Uuid

class EmployeeServiceTest : KoinComponent {

    @BeforeTest
    fun setUp() {
        TestUtils.loadSettings()
        TestUtils.setupDatabase()
        TestUtils.setupKoin(
            modules = listOf(
                RbacDomainInjection.get(),
                ActorDomainInjection.get(),
                EmployeeDomainInjection.get()
            )
        )
    }

    @AfterTest
    fun tearDown() {
        TestUtils.tearDown()
    }

    @Test
    fun testGetEmployee(): Unit = testSuspend {
        MaritalStatus.entries.forEachIndexed { index, maritalStatus ->
            Honorific.entries.forEach { honorific ->

                val timestamp: Instant = Clock.System.now()
                val dob = LocalDate(year = 2000, monthNumber = 1, dayOfMonth = 1 + index)
                val firstName = "AnyName_$index"
                val lastName = "AnySurname_$index"
                val employeeId: Uuid = Uuid.random()

                val mockEmployee = Employee(
                    id = employeeId,
                    firstName = firstName,
                    lastName = lastName,
                    fullName = "$lastName, $firstName",
                    workEmail = "$firstName.$lastName@work.com",
                    dob = dob,
                    age = dob.age(),
                    honorific = honorific,
                    maritalStatus = maritalStatus,
                    contact = Contact(
                        id = Uuid.random(),
                        email = "$firstName.$lastName@public.com",
                        phone = "+34-611-222-333",
                        meta = Meta(
                            createdAt = timestamp,
                            updatedAt = timestamp
                        )
                    ),
                    meta = Meta(
                        createdAt = timestamp,
                        updatedAt = timestamp
                    )
                )

                assert(value = mockEmployee.age != 0)
                assert(value = mockEmployee.fullName.isNotBlank())

                val sessionContext: SessionContext = mockk<SessionContext>()
                every { sessionContext.schema } returns null
                every { sessionContext.db } returns null

                val mockEmployeeRepository = mockk<IEmployeeRepository>()
                val employeeService = EmployeeService(
                    sessionContext = sessionContext,
                    employeeRepository = mockEmployeeRepository
                )
                coEvery { mockEmployeeRepository.findById(employeeId = employeeId) } returns mockEmployee

                val result: Employee? = employeeService.findById(employeeId = employeeId)
                assertEquals(
                    expected = mockEmployee,
                    actual = result,
                    message = "Employee should be equal to mockEmployee."
                )
            }
        }
    }

    @Test
    fun testCreateUpdateEmployee(): Unit = testSuspend {
        val employeeRequest: EmployeeRequest = EmployeeTestUtils.newEmployeeRequest()

        newSuspendedTransaction {
            val sessionContext: SessionContext = mockk<SessionContext>()
            every { sessionContext.schema } returns null
            every { sessionContext.db } returns null

            val employeeService: EmployeeService by inject(
                parameters = { parametersOf(sessionContext) }
            )

            // Create
            val employee: Employee = employeeService.create(request = employeeRequest).getOrThrow()
            assertEquals(expected = employeeRequest.firstName, actual = employee.firstName)

            // Update
            MaritalStatus.entries.forEachIndexed { index, maritalStatus ->
                val updateEmployeeRequest = EmployeeRequest(
                    firstName = "AnyName_$index",
                    lastName = "AnySurname_$index",
                    workEmail = "AnyName.AnySurname.$index@work.com",
                    dob = LocalDate(year = 2000, monthNumber = 1, dayOfMonth = 1 + index),
                    honorific = Honorific.MR,
                    maritalStatus = maritalStatus
                )

                val updatedEmployee: Employee? = employeeService.update(
                    employeeId = employee.id,
                    request = updateEmployeeRequest
                ).getOrNull()

                assertNotNull(
                    actual = updatedEmployee,
                    message = "Updated employee should not be null."
                )
                assertEquals(
                    expected = updateEmployeeRequest.firstName,
                    actual = updatedEmployee.firstName,
                    message = "First name should be ${updateEmployeeRequest.firstName}."
                )
                assertEquals(
                    expected = updateEmployeeRequest.lastName,
                    actual = updatedEmployee.lastName,
                    message = "Last name should be ${updateEmployeeRequest.lastName}."
                )
                assertEquals(
                    expected = updateEmployeeRequest.dob,
                    actual = updatedEmployee.dob,
                    message = "DOB should be ${updateEmployeeRequest.dob}."
                )
                assertEquals(
                    expected = updateEmployeeRequest.maritalStatus,
                    actual = updatedEmployee.maritalStatus,
                    message = "Marital status should be ${updateEmployeeRequest.maritalStatus}."
                )
            }

            rollback()

            val page: Page<Employee> = employeeService.findAll()
            assertEquals(
                expected = 0,
                actual = page.details.totalElements,
                message = "After rollback total elements should be 0."
            )
        }
    }
}
