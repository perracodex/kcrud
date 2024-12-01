/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */
import io.ktor.test.dispatcher.*
import io.mockk.every
import io.mockk.mockk
import io.perracodex.exposed.pagination.Page
import kcrud.access.domain.actor.di.ActorDomainInjection
import kcrud.access.domain.rbac.di.RbacDomainInjection
import kcrud.core.context.SessionContext
import kcrud.core.test.TestUtils
import kcrud.database.schema.employee.type.Honorific
import kcrud.database.schema.employee.type.MaritalStatus
import kcrud.database.test.DatabaseTestUtils
import kcrud.domain.employee.di.EmployeeDomainInjection
import kcrud.domain.employee.model.Employee
import kcrud.domain.employee.model.EmployeeRequest
import kcrud.domain.employee.service.EmployeeService
import kcrud.domain.employee.test.EmployeeTestUtils
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class StressTest : KoinComponent {

    @BeforeTest
    fun setUp() {
        TestUtils.loadSettings()
        DatabaseTestUtils.setupDatabase()
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
        DatabaseTestUtils.closeDatabase()
        TestUtils.tearDown()
    }

    @Test
    fun largeConcurrentSet(): Unit = testSuspend {
        val sessionContext: SessionContext = mockk<SessionContext>()
        every { sessionContext.schema } returns null
        every { sessionContext.db } returns null

        val employeeService: EmployeeService by inject(
            parameters = { parametersOf(sessionContext) }
        )

        val employeeRequest: EmployeeRequest = EmployeeTestUtils.newEmployeeRequest()
        val totalElements = 10000

        val jobs: List<Deferred<Employee>> = List(size = totalElements) { index ->
            val randomYears = (20..65).random()
            val randomMonths = (1..12).random()
            val randomDays = (1..28).random()
            val randomChars = List(size = 2) { "abc0123".random() }.joinToString(separator = "")

            val request = employeeRequest.copy(
                firstName = "${employeeRequest.firstName}_$index",
                maritalStatus = MaritalStatus.entries.random(),
                honorific = Honorific.entries.random(),
                dob = employeeRequest.dob.minus(randomYears, DateTimeUnit.YEAR)
                    .minus(randomMonths, DateTimeUnit.MONTH)
                    .minus(randomDays, DateTimeUnit.DAY),
                contact = employeeRequest.contact?.let { contactRequest ->
                    contactRequest.copy(email = "${randomChars}_${contactRequest.email}")
                }
            )

            async {
                employeeService.create(request = request).getOrThrow()
            }
        }

        // Await all the Deferred objects to ensure all async operations complete.
        jobs.awaitAll()

        // Verify all employees after all insertions are complete.
        val employees: Page<Employee> = employeeService.findAll()
        assertEquals(expected = totalElements, actual = employees.content.size)
        assertEquals(expected = totalElements, actual = employees.details.totalElements)

        employeeService.deleteAll()
    }

    @Test
    fun largeEmployeeSet(): Unit = testSuspend {
        val sessionContext: SessionContext = mockk<SessionContext>()
        every { sessionContext.schema } returns null
        every { sessionContext.db } returns null

        val employeeService: EmployeeService by inject(
            parameters = { parametersOf(sessionContext) }
        )

        val employeeRequest: EmployeeRequest = EmployeeTestUtils.newEmployeeRequest()
        val totalElements = 10000

        (1..totalElements).forEach { index ->
            val randomYears = (20..65).random()
            val randomMonths = (1..12).random()
            val randomDays = (1..28).random()
            val randomChars = List(size = 2) { "abc0123".random() }.joinToString(separator = "")

            val request = employeeRequest.copy(
                firstName = "${employeeRequest.firstName}_$index",
                maritalStatus = MaritalStatus.entries.random(),
                honorific = Honorific.entries.random(),
                dob = employeeRequest.dob.minus(randomYears, DateTimeUnit.YEAR)
                    .minus(randomMonths, DateTimeUnit.MONTH)
                    .minus(randomDays, DateTimeUnit.DAY),
                contact = employeeRequest.contact?.let { contactRequest ->
                    contactRequest.copy(email = "${randomChars}_${contactRequest.email}")
                }
            )

            employeeService.create(request = request).getOrThrow()
        }

        // Verify all employees.
        val employees: Page<Employee> = employeeService.findAll()
        assertEquals(expected = totalElements, actual = employees.content.size)
        assertEquals(expected = totalElements, actual = employees.details.totalElements)
    }
}
