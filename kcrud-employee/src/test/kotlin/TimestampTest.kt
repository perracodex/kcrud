/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

import io.ktor.test.dispatcher.*
import io.mockk.every
import io.mockk.mockk
import kcrud.base.database.schema.employee.types.Honorific
import kcrud.base.database.schema.employee.types.MaritalStatus
import kcrud.base.env.SessionContext
import kcrud.base.persistence.serializers.OffsetTimestamp
import kcrud.base.utils.KLocalDate
import kcrud.base.utils.TestUtils
import kcrud.domain.employee.di.EmployeeDomainInjection
import kcrud.domain.employee.model.EmployeeDto
import kcrud.domain.employee.model.EmployeeRequest
import kcrud.domain.employee.repository.IEmployeeRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import kotlin.test.*
import kotlin.uuid.Uuid


class TimestampTest : KoinComponent {

    @BeforeTest
    fun setUp() {
        TestUtils.loadSettings()
        TestUtils.setupDatabase()
        TestUtils.setupKoin(modules = listOf(EmployeeDomainInjection.get()))
    }

    @AfterTest
    fun tearDown() {
        TestUtils.tearDown()
    }

    @Test
    fun testTimestamp(): Unit = testSuspend {
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

        val employeeId: Uuid = employeeRepository.create(employeeRequest = employeeRequest)
        val employee: EmployeeDto = employeeRepository.findById(employeeId = employeeId)!!

        // Assert that the record has timestamps.
        assertNotNull(actual = employee)
        assertNotNull(actual = employee.meta.createdAt)
        assertNotNull(actual = employee.meta.updatedAt)

        // Assert that both timestamps are the same after creation.
        assertEquals(
            expected = employee.meta.createdAt,
            actual = employee.meta.updatedAt
        )

        val createdAt: OffsetTimestamp = employee.meta.createdAt
        val updatedAt: OffsetTimestamp = employee.meta.updatedAt
        employeeRepository.update(employeeId = employeeId, employeeRequest = employeeRequest)
        val updatedEmployee: EmployeeDto = employeeRepository.findById(employeeId = employeeId)!!

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
