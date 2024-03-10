/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

import io.ktor.test.dispatcher.*
import io.mockk.every
import io.mockk.mockk
import kcrud.base.database.schema.employee.types.Honorific
import kcrud.base.database.schema.employee.types.MaritalStatus
import kcrud.base.infrastructure.env.SessionContext
import kcrud.base.infrastructure.utils.KLocalDate
import kcrud.base.infrastructure.utils.TestUtils
import kcrud.base.persistence.pagination.Page
import kcrud.base.persistence.pagination.Pageable
import kcrud.domain.contact.entities.ContactRequest
import kcrud.domain.employee.entities.EmployeeEntity
import kcrud.domain.employee.entities.EmployeeRequest
import kcrud.domain.employee.injection.EmployeeInjection
import kcrud.domain.employee.repository.IEmployeeRepository
import kcrud.domain.employee.service.EmployeeService
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PaginationTest : KoinComponent {

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
    fun testEmptyPagination() = testSuspend {
        newSuspendedTransaction {
            val sessionContext: SessionContext = mockk<SessionContext>()
            every { sessionContext.schema } returns null

            val employeeService: EmployeeService by inject(
                parameters = { parametersOf(sessionContext) }
            )

            val totalRecords = 10

            // No records.
            employeeService.findAll(pageable = Pageable(page = 1, size = totalRecords)).also { page ->
                assertEquals(
                    expected = 1,
                    actual = page.totalPages,
                    message = "Total pages should be 1."
                )
                assertEquals(
                    expected = 0,
                    actual = page.totalElements,
                    message = "Total elements should be 0."
                )
                assertEquals(
                    expected = totalRecords,
                    actual = page.elementsPerPage,
                    message = "Elements per page should be $totalRecords."
                )
                assertEquals(
                    expected = 0,
                    actual = page.elementsInPage,
                    message = "Elements in page should be 0."
                )
                assertEquals(
                    expected = 1,
                    actual = page.pageIndex,
                    message = "Page index should be 1."
                )
                assertEquals(
                    expected = false,
                    actual = page.hasNext,
                    message = "Has next should be false."
                )
                assertEquals(
                    expected = false,
                    actual = page.hasPrevious,
                    message = "Has previous should be false."
                )
                assertEquals(
                    expected = true,
                    actual = page.isFirst,
                    message = "Is first should be true."
                )
                assertEquals(
                    expected = true,
                    actual = page.isLast,
                    message = "Is last should be true."
                )
            }
        }
    }

    @Test
    fun testEvenPagination() = testSuspend {
        newSuspendedTransaction {
            val sessionContext: SessionContext = mockk<SessionContext>()
            every { sessionContext.schema } returns null

            val employeeService: EmployeeService by inject(
                parameters = { parametersOf(sessionContext) }
            )

            val totalRecords = 10

            // Create test records.
            repeat(times = totalRecords) {
                val employeeRequest = createEmployeeRequest()
                employeeService.create(employeeRequest = employeeRequest)
            }

            // 1 Page
            employeeService.findAll(pageable = Pageable(page = 1, size = 0)).also { page ->
                assertEquals(
                    expected = 1,
                    actual = page.totalPages,
                    message = "Total pages should be 1."
                )
                assertEquals(
                    expected = totalRecords,
                    actual = page.totalElements,
                    message = "Total elements should be $totalRecords."
                )
                assertEquals(
                    expected = totalRecords,
                    actual = page.elementsPerPage,
                    message = "Elements per page should be $totalRecords."
                )
                assertEquals(
                    expected = totalRecords,
                    actual = page.elementsInPage,
                    message = "Elements in page should be $totalRecords."
                )
                assertEquals(
                    expected = 1,
                    actual = page.pageIndex,
                    message = "Page index should be 1."
                )
                assertEquals(
                    expected = false,
                    actual = page.hasNext,
                    message = "Has next should be false."
                )
                assertEquals(
                    expected = false,
                    actual = page.hasPrevious,
                    message = "Has previous should be false."
                )
                assertEquals(
                    expected = true,
                    actual = page.isFirst,
                    message = "Is first should be true."
                )
                assertEquals(
                    expected = true,
                    actual = page.isLast,
                    message = "Is last should be true."
                )
            }

            // 2 pages.
            val halfPageCount = totalRecords / 2
            employeeService.findAll(pageable = Pageable(page = 1, size = halfPageCount)).also { page ->
                assertEquals(
                    expected = 2,
                    actual = page.totalPages,
                    message = "Total pages should be 2."
                )

                assertEquals(
                    expected = totalRecords,
                    actual = page.totalElements,
                    message = "Total elements should be $totalRecords."
                )

                assertEquals(
                    expected = halfPageCount,
                    actual = page.elementsPerPage,
                    message = "Elements per page should be $halfPageCount."
                )

                assertEquals(
                    expected = halfPageCount,
                    actual = page.elementsInPage,
                    message = "Elements in page should be $halfPageCount."
                )

                assertEquals(
                    expected = 1,
                    actual = page.pageIndex,
                    message = "Page index should be 1."
                )

                assertEquals(
                    expected = true,
                    actual = page.hasNext,
                    message = "Has next should be true."
                )

                assertEquals(
                    expected = false,
                    actual = page.hasPrevious,
                    message = "Has previous should be false."
                )

                assertEquals(
                    expected = true,
                    actual = page.isFirst,
                    message = "Is first should be true."
                )

                assertEquals(
                    expected = false,
                    actual = page.isLast,
                    message = "Is last should be false."
                )
            }

            rollback()

            val page: Page<EmployeeEntity> = employeeService.findAll()
            assertEquals(
                expected = 0, actual = page.totalElements,
                message = "After rollback total elements should be 0."
            )
        }
    }

    @Test
    fun testOddPagination() = testSuspend {
        newSuspendedTransaction {
            val sessionContext: SessionContext = mockk<SessionContext>()
            every { sessionContext.schema } returns null

            val employeeService: EmployeeService by inject(
                parameters = { parametersOf(sessionContext) }
            )

            val totalRecords = 12

            // Create test records.
            repeat(times = totalRecords) {
                val employeeRequest = createEmployeeRequest()
                employeeService.create(employeeRequest = employeeRequest)
            }

            val elementsPerPage = 5

            (1..3).forEach { pageIndex ->
                employeeService.findAll(pageable = Pageable(page = pageIndex, size = elementsPerPage)).also { page ->
                    assertEquals(
                        expected = 3,
                        actual = page.totalPages,
                        message = "Total pages should be 3."
                    )
                    assertEquals(
                        expected = totalRecords,
                        actual = page.totalElements,
                        message = "Total elements should be $totalRecords."
                    )
                    assertEquals(
                        expected = elementsPerPage,
                        actual = page.elementsPerPage,
                        message = "Elements per page should be $elementsPerPage."
                    )

                    when (pageIndex) {
                        1 -> {
                            assertEquals(
                                expected = elementsPerPage,
                                actual = page.elementsInPage,
                                message = "Elements in page should be $elementsPerPage."
                            )
                            assertEquals(
                                expected = true,
                                actual = page.hasNext,
                                message = "Has next should be true."
                            )
                            assertEquals(
                                expected = false,
                                actual = page.hasPrevious,
                                message = "Has previous should be false."
                            )
                            assertEquals(
                                expected = true,
                                actual = page.isFirst,
                                message = "Is first should be true."
                            )
                            assertEquals(
                                expected = false,
                                actual = page.isLast,
                                message = "Is last should be false."
                            )
                        }

                        2 -> {
                            assertEquals(
                                expected = elementsPerPage,
                                actual = page.elementsInPage,
                                message = "Elements in page should be $elementsPerPage."
                            )
                            assertEquals(
                                expected = true,
                                actual = page.hasNext,
                                message = "Has next should be true."
                            )
                            assertEquals(
                                expected = true,
                                actual = page.hasPrevious,
                                message = "Has previous should be true."
                            )
                            assertEquals(
                                expected = false,
                                actual = page.isFirst,
                                message = "Is first should be false."
                            )
                            assertEquals(
                                expected = false,
                                actual = page.isLast,
                                message = "Is last should be false."
                            )
                        }

                        3 -> {
                            assertEquals(
                                expected = 2,
                                actual = page.elementsInPage,
                                message = "Elements in page should be 2."
                            )
                            assertEquals(
                                expected = false,
                                actual = page.hasNext,
                                message = "Has next should be false."
                            )
                            assertEquals(
                                expected = true,
                                actual = page.hasPrevious,
                                message = "Has previous should be true."
                            )
                            assertEquals(
                                expected = false,
                                actual = page.isFirst,
                                message = "Is first should be false."
                            )
                            assertEquals(
                                expected = true,
                                actual = page.isLast,
                                message = "Is last should be true."
                            )
                        }
                    }
                }
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

    @Test
    fun testPaginationCount() = testSuspend {
        newSuspendedTransaction {
            val sessionContext: SessionContext = mockk<SessionContext>()
            every { sessionContext.schema } returns null

            val employeeRepository: IEmployeeRepository by inject(
                parameters = { parametersOf(sessionContext) }
            )

            val totalRecords = 10

            repeat(times = totalRecords) {
                val employeeRequest = createEmployeeRequest()
                employeeRepository.create(employeeRequest)
            }

            employeeRepository.findAll().also { page ->
                assertEquals(
                    expected = totalRecords,
                    actual = page.totalElements,
                    message = "There must be 10 employees in the database."
                )
                assertEquals(
                    expected = employeeRepository.count(),
                    actual = totalRecords,
                    message = "There must be 10 employees in the database."
                )
            }

            rollback()

            employeeRepository.findAll().also { page ->
                assertEquals(
                    expected = 0,
                    actual = page.totalElements,
                    message = "There must be 10 employees in the database."
                )
            }
        }
    }

    @Test
    fun testRandomPagination() = testSuspend {
        newSuspendedTransaction {
            val sessionContext: SessionContext = mockk<SessionContext>()
            every { sessionContext.schema } returns null

            val employeeService: EmployeeService by inject(
                parameters = { parametersOf(sessionContext) }
            )

            val totalRecords = 1000
            val names: List<String> = List(totalRecords) { "Employee_${it + 1}" } // Create identifiable names.

            // Create test records.
            names.forEachIndexed { index, name ->
                val employeeRequest = EmployeeRequest(
                    firstName = name,
                    lastName = "Surname_$index",
                    dob = KLocalDate(year = 2000, monthNumber = 1, dayOfMonth = (index % 28) + 1), // Ensure valid day of month.
                    honorific = Honorific.entries.random(),
                    maritalStatus = MaritalStatus.entries.random()
                )

                employeeService.create(employeeRequest = employeeRequest)
            }

            val random = kotlin.random.Random

            // Test random pagination.
            (1..25).forEach { _ ->
                val pageSize: Int = random.nextInt(from = 1, until = 50)
                val pageIndex: Int = random.nextInt(from = 1, until = (totalRecords / pageSize) + 1)

                employeeService.findAll(pageable = Pageable(page = pageIndex, size = pageSize)).also { page ->
                    val expectedTotalPages: Int = (totalRecords + pageSize - 1) / pageSize // Calculate expected total pages.
                    val startIndex: Int = (pageIndex - 1) * pageSize // Calculate the start index of the records for the current page.

                    assertEquals(
                        expected = expectedTotalPages,
                        actual = page.totalPages,
                        message = "Total pages should be $expectedTotalPages."
                    )
                    assertEquals(
                        expected = totalRecords,
                        actual = page.totalElements,
                        message = "Total elements should be $totalRecords."
                    )
                    assertEquals(
                        expected = pageSize,
                        actual = page.elementsPerPage,
                        message = "Elements per page should be $pageSize."
                    )
                    assertEquals(
                        expected = minOf(pageSize, totalRecords - startIndex),
                        actual = page.elementsInPage,
                        message = "Elements in page should be ${minOf(pageSize, totalRecords - startIndex)}."
                    )
                    assertEquals(
                        expected = pageIndex,
                        actual = page.pageIndex,
                        message = "Page index should be $pageIndex."
                    )

                    // Verify the actual records on the page.
                    val expectedNames: List<String> = names.subList(startIndex, minOf(startIndex + pageSize, totalRecords))
                    val actualNames: List<String> = page.content.map { it.firstName }
                    assertEquals(
                        expected = expectedNames,
                        actual = actualNames,
                        message = "Employee names on page $pageIndex should match expected."
                    )
                }
            }

            rollback()
        }
    }

    @Test
    fun testRandomPaginationWithSorting() = testSuspend {
        newSuspendedTransaction {
            val sessionContext: SessionContext = mockk<SessionContext>()
            every { sessionContext.schema } returns null

            val employeeService: EmployeeService by inject(
                parameters = { parametersOf(sessionContext) }
            )

            val totalRecords = 1000
            val names: List<String> = List(totalRecords) { "Employee_${it + 1}" }

            // Create test records and store them for later validation.
            val createdEmployees = mutableListOf<EmployeeEntity>()
            names.forEachIndexed { index, name ->
                val employeeRequest = EmployeeRequest(
                    firstName = name,
                    lastName = "Surname_$index",
                    dob = KLocalDate(year = 2000, monthNumber = 1, dayOfMonth = (index % 28) + 1),
                    honorific = Honorific.MS,
                    maritalStatus = MaritalStatus.SINGLE
                )
                val createdEmployee = employeeService.create(employeeRequest = employeeRequest)
                createdEmployees.add(createdEmployee)
            }

            val random = kotlin.random.Random
            val sortFields = listOf("firstName", "lastName", "honorific", "maritalStatus", "dob")

            (1..25).forEach { _ ->
                val pageSize: Int = random.nextInt(from = 1, until = 50)
                val pageIndex: Int = random.nextInt(from = 1, until = (totalRecords / pageSize) + 1)
                val sortField: String = sortFields.random()
                val sortOrder: Pageable.Direction = Pageable.Direction.entries.random()

                val pageable = Pageable(
                    page = pageIndex,
                    size = pageSize,
                    sort = listOf(Pageable.Sort(field = sortField, direction = sortOrder))
                )

                // Sort createdEmployees for validation according to the same criteria
                val sortedEmployees = when (sortField) {
                    "firstName", "lastName" -> {
                        if (sortOrder == Pageable.Direction.ASC) {
                            createdEmployees.sortedBy { it.getSortValue(sortField) as String }
                        } else {
                            createdEmployees.sortedByDescending { it.getSortValue(sortField) as String }
                        }
                    }

                    "honorific", "maritalStatus" -> {
                        if (sortOrder == Pageable.Direction.ASC) {
                            createdEmployees.sortedBy { (it.getSortValue(sortField) as Enum<*>).name }
                        } else {
                            createdEmployees.sortedByDescending { (it.getSortValue(sortField) as Enum<*>).name }
                        }
                    }

                    "dob" -> {
                        if (sortOrder == Pageable.Direction.ASC) {
                            createdEmployees.sortedBy { it.getSortValue(sortField) as KLocalDate }
                        } else {
                            createdEmployees.sortedByDescending { it.getSortValue(sortField) as KLocalDate }
                        }
                    }

                    else -> throw IllegalArgumentException("Unknown sort field: $sortField")
                }

                employeeService.findAll(pageable = pageable).also { page ->
                    val expectedTotalPages: Int = (totalRecords + pageSize - 1) / pageSize
                    val expectedRecordsForPage = sortedEmployees
                        .drop(n = (pageIndex - 1) * pageSize)
                        .take(pageSize)

                    assertEquals(
                        expected = expectedTotalPages,
                        actual = page.totalPages,
                        message = "Total pages should be $expectedTotalPages."
                    )
                    assertEquals(
                        expected = totalRecords,
                        actual = page.totalElements,
                        message = "Total elements should be $totalRecords."
                    )
                    assertEquals(
                        expected = pageSize,
                        actual = page.elementsPerPage,
                        message = "Elements per page should be $pageSize."
                    )
                    assertEquals(
                        expected = minOf(pageSize, totalRecords - ((pageIndex - 1) * pageSize)),
                        actual = page.elementsInPage,
                        message = "Elements in page should be ${minOf(pageSize, totalRecords - ((pageIndex - 1) * pageSize))}."
                    )
                    assertEquals(
                        expected = pageIndex,
                        actual = page.pageIndex,
                        message = "Page index should be $pageIndex."
                    )

                    // Verify the actual records on the page are sorted and match the expected slice from sortedEmployees.
                    val actualValues = page.content.map { it.getSortValue(sortField) }
                    val expectedValues = expectedRecordsForPage.map { it.getSortValue(sortField) }

                    if (sortField == "dob") {
                        // Cast to LocalDate for comparison if the field is a date.
                        val actualDates = actualValues as List<*>
                        val expectedDates = expectedValues as List<*>
                        assertEquals(
                            expected = expectedDates,
                            actual = actualDates,
                            message = "Dates on page $pageIndex should match the expected dates."
                        )
                    } else {
                        // For string comparisons, including enums.
                        val actualStrings = actualValues.map { it.toString() }
                        val expectedStrings = expectedValues.map { it.toString() }
                        assertEquals(
                            expected = expectedStrings,
                            actual = actualStrings,
                            message = "Records on page $pageIndex should be correctly sorted by $sortField in ${sortOrder.name} " +
                                    "order and match the expected records."
                        )
                    }
                }
            }

            rollback()
        }
    }

    private fun EmployeeEntity.getSortValue(sortField: String): Comparable<*> {
        return when (sortField) {
            "firstName" -> this.firstName
            "lastName" -> this.lastName
            "honorific" -> this.honorific
            "maritalStatus" -> this.maritalStatus
            "dob" -> this.dob
            else -> throw IllegalArgumentException("Unknown sort field: $sortField")
        }
    }

    private fun createEmployeeRequest(): EmployeeRequest {
        val firstName = TestUtils.randomName()
        val lastName = TestUtils.randomName()
        return EmployeeRequest(
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
    }
}
