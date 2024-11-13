/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

import io.ktor.test.dispatcher.*
import io.mockk.every
import io.mockk.mockk
import kcrud.access.actor.di.ActorDomainInjection
import kcrud.access.rbac.di.RbacDomainInjection
import kcrud.core.context.SessionContext
import kcrud.core.database.schema.employee.type.Honorific
import kcrud.core.database.schema.employee.type.MaritalStatus
import kcrud.core.error.validator.base.ValidationException
import kcrud.core.test.TestUtils
import kcrud.domain.contact.model.ContactRequest
import kcrud.domain.contact.repository.IContactRepository
import kcrud.domain.employee.di.EmployeeDomainInjection
import kcrud.domain.employee.model.EmployeeRequest
import kcrud.domain.employee.repository.IEmployeeRepository
import kcrud.domain.employee.test.EmployeeTestUtils
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import kotlin.test.*

class TransactionTest : KoinComponent {

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
    fun testNestedTransaction(): Unit = testSuspend {
        val sessionContext: SessionContext = mockk<SessionContext>()
        every { sessionContext.schema } returns null
        every { sessionContext.db } returns null

        val contactRepository: IContactRepository by inject(
            parameters = { parametersOf(sessionContext) }
        )
        val employeeRepository: IEmployeeRepository by inject(
            parameters = { parametersOf(sessionContext) }
        )

        val employeeRequest: EmployeeRequest = EmployeeTestUtils.newEmployeeRequest()

        newSuspendedTransaction {
            assertEquals(
                expected = 0,
                actual = employeeRepository.count(),
                message = "There must be 0 employees in the database."
            )
            assertEquals(
                expected = 0,
                actual = contactRepository.count(),
                message = "There must be 0 contacts in the database."
            )

            employeeRepository.create(request = employeeRequest)

            assertEquals(
                expected = 1,
                actual = employeeRepository.count(),
                message = "There must be 1 employee in the database."
            )
            assertEquals(
                expected = 1,
                actual = contactRepository.count(),
                message = "There must be 1 contact in the database."
            )

            // Rollback the transaction, so no data is persisted.
            // After this, the database must be empty again.
            rollback()

            assertEquals(
                expected = 0,
                actual = employeeRepository.count(),
                message = "There must be 0 employees in the database."
            )
            assertEquals(
                expected = 0,
                actual = contactRepository.count(),
                message = "There must be 0 contacts in the database."
            )
        }
    }

    /**
     * Similar to [testNestedTransaction], but this time an invalid phone
     * is provided, so the transaction must be rolled back on error.
     */
    @Test
    fun testNestedTransactionRollbackByError(): Unit = testSuspend {
        val sessionContext: SessionContext = mockk<SessionContext>()
        every { sessionContext.schema } returns null
        every { sessionContext.db } returns null

        val contactRepository: IContactRepository by inject(
            parameters = { parametersOf(sessionContext) }
        )
        val employeeRepository: IEmployeeRepository by inject(
            parameters = { parametersOf(sessionContext) }
        )

        assertFailsWith<ValidationException> {
            transaction {
                // Create an employee with a valid contact detail.
                employeeRepository.create(request = EmployeeTestUtils.newEmployeeRequest())

                assertEquals(
                    expected = 1,
                    actual = contactRepository.count(),
                    message = "There must be 1 employees in the database."
                )

                // Try to create an employee with an invalid contact detail to make the transaction fail.
                // Done in an even one more nested transaction to test how it behaves.
                // Expected all the transaction tree to be rolled back.
                transaction {
                    val invalidEmployeeRequest = EmployeeRequest(
                        firstName = "AnyName",
                        lastName = "AnySurname",
                        dob = LocalDate(year = 2000, monthNumber = 1, dayOfMonth = 1),
                        workEmail = "X".repeat(100), // Invalid email length..
                        honorific = Honorific.MR,
                        maritalStatus = MaritalStatus.SINGLE,
                        contact = ContactRequest(
                            email = "X".repeat(100), // Invalid email length..
                            phone = "X".repeat(100), // Invalid phone length.
                        )
                    )

                    employeeRepository.create(request = invalidEmployeeRequest)
                }
            }
        }

        // After the exception, the transaction must be rolled back, so no data is persisted.
        assertEquals(
            expected = 0,
            actual = employeeRepository.count(),
            message = "There must be 0 employees in the database."
        )
        assertEquals(
            expected = 0,
            actual = contactRepository.count(),
            message = "There must be 0 contacts in the database."
        )
    }
}
