/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

import io.ktor.test.dispatcher.*
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kcrud.access.actor.di.ActorDomainInjection
import kcrud.access.rbac.di.RbacDomainInjection
import kcrud.base.database.schema.employee.types.Honorific
import kcrud.base.database.schema.employee.types.MaritalStatus
import kcrud.base.env.SessionContext
import kcrud.base.persistence.model.Meta
import kcrud.base.persistence.pagination.Page
import kcrud.base.persistence.serializers.OffsetTimestamp
import kcrud.base.utils.DateTimeUtils
import kcrud.base.utils.KLocalDate
import kcrud.base.utils.TestUtils
import kcrud.domain.contact.model.Contact
import kcrud.domain.employee.di.EmployeeDomainInjection
import kcrud.domain.employee.model.Employee
import kcrud.domain.employee.model.EmployeeRequest
import kcrud.domain.employee.repository.IEmployeeRepository
import kcrud.domain.employee.service.EmployeeService
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
        TestUtils.setupKoin(modules = listOf(RbacDomainInjection.get(), ActorDomainInjection.get(), EmployeeDomainInjection.get()))
    }

    @AfterTest
    fun tearDown() {
        TestUtils.tearDown()
    }

    @Test
    fun testGetEmployee(): Unit = testSuspend {
        MaritalStatus.entries.forEachIndexed { index, maritalStatus ->
            Honorific.entries.forEach { honorific ->

                val currentDateTime: OffsetTimestamp = DateTimeUtils.currentZonedTimestamp()
                val dob: KLocalDate = KLocalDate(year = 2000, monthNumber = 1, dayOfMonth = 1 + index)
                val firstName = "AnyName_$index"
                val lastName = "AnySurname_$index"
                val employeeId: Uuid = Uuid.random()

                val mockEmployee = Employee(
                    id = employeeId,
                    firstName = firstName,
                    lastName = lastName,
                    fullName = "$lastName, $firstName",
                    dob = dob,
                    age = DateTimeUtils.age(dob = dob),
                    honorific = honorific,
                    maritalStatus = maritalStatus,
                    contact = Contact(
                        id = Uuid.random(),
                        email = "$firstName.$lastName@kcrud.com",
                        phone = "+34-611-222-333",
                        meta = Meta(
                            createdAt = currentDateTime,
                            updatedAt = currentDateTime
                        )
                    ),
                    meta = Meta(
                        createdAt = currentDateTime,
                        updatedAt = currentDateTime
                    )
                )

                assert(value = mockEmployee.age != 0)
                assert(value = mockEmployee.fullName.isNotBlank())

                val sessionContext: SessionContext = mockk<SessionContext>()
                every { sessionContext.schema } returns null

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
        val employeeRequest = EmployeeRequest(
            firstName = "AnyName",
            lastName = "AnySurname",
            dob = KLocalDate(year = 2000, monthNumber = 1, dayOfMonth = 1),
            honorific = Honorific.MR,
            maritalStatus = MaritalStatus.MARRIED
        )

        newSuspendedTransaction {
            val sessionContext: SessionContext = mockk<SessionContext>()
            every { sessionContext.schema } returns null

            val employeeService: EmployeeService by inject(
                parameters = { parametersOf(sessionContext) }
            )

            // Create
            val employee: Employee = employeeService.create(employeeRequest = employeeRequest)
            assertEquals(expected = employeeRequest.firstName, actual = employee.firstName)

            // Update
            MaritalStatus.entries.forEachIndexed { index, maritalStatus ->
                val updateEmployeeRequest = EmployeeRequest(
                    firstName = "AnyName_$index",
                    lastName = "AnySurname_$index",
                    dob = KLocalDate(year = 2000, monthNumber = 1, dayOfMonth = 1 + index),
                    honorific = Honorific.MR,
                    maritalStatus = maritalStatus
                )

                val updatedEmployee: Employee? = employeeService.update(
                    employeeId = employee.id,
                    employeeRequest = updateEmployeeRequest
                )

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
                actual = page.totalElements,
                message = "After rollback total elements should be 0."
            )
        }
    }
}
