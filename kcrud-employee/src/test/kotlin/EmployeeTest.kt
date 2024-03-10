/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

import io.ktor.test.dispatcher.*
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kcrud.base.database.schema.employee.types.Honorific
import kcrud.base.database.schema.employee.types.MaritalStatus
import kcrud.base.infrastructure.env.SessionContext
import kcrud.base.infrastructure.utils.KLocalDate
import kcrud.base.infrastructure.utils.TestUtils
import kcrud.base.persistence.pagination.Page
import kcrud.domain.contact.entities.ContactEntity
import kcrud.domain.employee.entities.EmployeeEntity
import kcrud.domain.employee.entities.EmployeeRequest
import kcrud.domain.employee.injection.EmployeeInjection
import kcrud.domain.employee.repository.IEmployeeRepository
import kcrud.domain.employee.service.EmployeeService
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import java.util.*
import kotlin.test.*

class EmployeeEntityServiceTest : KoinComponent {

    @BeforeTest
    fun setUp() {
        TestUtils.loadSettings()
        TestUtils.setupDatabase()
        TestUtils.setupKoin(listOf(EmployeeInjection.get()))
    }

    @AfterTest
    fun tearDown() {
        TestUtils.tearDown()
    }

    @Test
    fun testGetEmployee() = testSuspend {
        MaritalStatus.entries.forEachIndexed { index, maritalStatus ->
            Honorific.entries.forEach { honorific ->

                val employeeId = UUID.randomUUID()
                val mockEmployee = EmployeeEntity(
                    id = employeeId,
                    firstName = "AnyName_$index",
                    lastName = "AnySurname_$index",
                    dob = KLocalDate(year = 2000, monthNumber = 1, dayOfMonth = 1 + index),
                    honorific = honorific,
                    maritalStatus = maritalStatus,
                    contact = ContactEntity(
                        id = UUID.randomUUID(),
                        email = "AnyName.AnySurname@email.com",
                        phone = "123-456-789"
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

                val result = employeeService.findById(employeeId = employeeId)
                assertEquals(
                    expected = mockEmployee,
                    actual = result,
                    message = "Employee should be equal to mockEmployee."
                )
            }
        }
    }

    @Test
    fun testCreateUpdateEmployee() = testSuspend {
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
            val employee: EmployeeEntity = employeeService.create(employeeRequest = employeeRequest)
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

                val updatedEmployee: EmployeeEntity? = employeeService.update(
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

            val page: Page<EmployeeEntity> = employeeService.findAll()
            assertEquals(
                expected = 0,
                actual = page.totalElements,
                message = "After rollback total elements should be 0."
            )
        }
    }
}
