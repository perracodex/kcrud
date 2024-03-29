/*
 * Copyright (c) 2024-Present Perracodex. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

import io.ktor.test.dispatcher.*
import io.mockk.every
import io.mockk.mockk
import kcrud.base.database.schema.employee.types.Honorific
import kcrud.base.database.schema.employee.types.MaritalStatus
import kcrud.base.env.SessionContext
import kcrud.base.utils.KLocalDate
import kcrud.base.utils.KLocalDateTime
import kcrud.base.utils.TestUtils
import kcrud.domain.employee.di.EmployeeInjection
import kcrud.domain.employee.entity.EmployeeEntity
import kcrud.domain.employee.entity.EmployeeRequest
import kcrud.domain.employee.repository.IEmployeeRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import java.util.*
import kotlin.test.*


class TimestampTest : KoinComponent {

    @BeforeTest
    fun setUp() {
        TestUtils.loadSettings()
        TestUtils.setupDatabase()
        TestUtils.setupKoin(modules = listOf(EmployeeInjection.get()))
    }

    @AfterTest
    fun tearDown() {
        TestUtils.tearDown()
    }

    /**
     * Test that data is not persisted when nested transactions are rolled back.
     * This test is required as we use a custom transaction solution to
     * support nested 'suspend' transactions.
     *
     * The test is done by creating an employee with a contact record.
     * Creating a contact is done in a nested transaction within employee
     * transaction as the parent.
     * This implies that this unit test will work with 3 transactions:
     * the outer one by the test, the middle one by the employee service,
     * and the inner one by the contact repository.
     *
     * It is expected that when rolling back in the top transaction,
     * no data is persisted in the database.
     */
    @Test
    fun testTimestamp() = testSuspend {
        val sessionContext: SessionContext = mockk<SessionContext>()
        every { sessionContext.schema } returns null

        val employeeRepository: IEmployeeRepository by inject(
            parameters = { parametersOf(sessionContext) }
        )

        val employeeRequest = EmployeeRequest(
            firstName = "AnyName",
            lastName = "AnySurname",
            dob = KLocalDate(year = 2000, monthNumber = 1, dayOfMonth = 1),
            honorific = Honorific.MR,
            maritalStatus = MaritalStatus.SINGLE
        )

        val employeeId: UUID = employeeRepository.create(employeeRequest = employeeRequest)
        val employee: EmployeeEntity = employeeRepository.findById(employeeId = employeeId)!!

        // Assert that the record has timestamps.
        assertNotNull(actual = employee)
        assertNotNull(actual = employee.meta.createdAt)
        assertNotNull(actual = employee.meta.updatedAt)

        // Assert that both timestamps are the same after creation.
        assertEquals(
            expected = employee.meta.createdAt,
            actual = employee.meta.updatedAt
        )

        val createdAt: KLocalDateTime = employee.meta.createdAt
        val updatedAt: KLocalDateTime = employee.meta.updatedAt
        employeeRepository.update(employeeId = employeeId, employeeRequest = employeeRequest)
        val updatedEmployee: EmployeeEntity = employeeRepository.findById(employeeId = employeeId)!!

        // The createdAt timestamp should not change.
        assertEquals(
            expected = createdAt,
            actual = updatedEmployee.meta.createdAt
        )

        // The updatedAt timestamp should change.
        assertNotEquals(
            illegal = updatedAt,
            actual = updatedEmployee.meta.updatedAt
        )
    }
}
