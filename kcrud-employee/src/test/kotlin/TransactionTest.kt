/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

import io.ktor.test.dispatcher.*
import io.mockk.every
import io.mockk.mockk
import kcrud.access.actor.injection.ActorInjection
import kcrud.access.rbac.injection.RbacInjection
import kcrud.access.system.SessionContext
import kcrud.base.database.schema.employee.types.Honorific
import kcrud.base.database.schema.employee.types.MaritalStatus
import kcrud.base.utils.KLocalDate
import kcrud.base.utils.TestUtils
import kcrud.domain.contact.entity.ContactRequest
import kcrud.domain.contact.repository.IContactRepository
import kcrud.domain.employee.entity.EmployeeRequest
import kcrud.domain.employee.injection.EmployeeInjection
import kcrud.domain.employee.repository.IEmployeeRepository
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
        TestUtils.setupKoin(modules = listOf(RbacInjection.get(), ActorInjection.get(), EmployeeInjection.get()))
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
    fun testNestedTransaction() = testSuspend {
        val sessionContext: SessionContext = mockk<SessionContext>()
        every { sessionContext.schema } returns null

        val contactRepository: IContactRepository by inject(
            parameters = { parametersOf(sessionContext) }
        )
        val employeeRepository: IEmployeeRepository by inject(
            parameters = { parametersOf(sessionContext) }
        )

        val employeeRequest = EmployeeRequest(
            firstName = "AnyName",
            lastName = "AnySurname",
            dob = KLocalDate(year = 2000, monthNumber = 1, dayOfMonth = 1),
            honorific = Honorific.MR,
            maritalStatus = MaritalStatus.SINGLE,
            contact = ContactRequest(
                email = "AnyName.AnySurname@email.com",
                phone = "+34-611-222-333"
            )
        )

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

            employeeRepository.create(employeeRequest = employeeRequest)

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
    fun testNestedTransactionRollbackByError() = testSuspend {
        val sessionContext: SessionContext = mockk<SessionContext>()
        every { sessionContext.schema } returns null

        val contactRepository: IContactRepository by inject(
            parameters = { parametersOf(sessionContext) }
        )
        val employeeRepository: IEmployeeRepository by inject(
            parameters = { parametersOf(sessionContext) }
        )

        // Create an employee with a valid contact detail.
        val employeeRequest = EmployeeRequest(
            firstName = "AnyName",
            lastName = "AnySurname",
            dob = KLocalDate(year = 2000, monthNumber = 1, dayOfMonth = 1),
            honorific = Honorific.MR,
            maritalStatus = MaritalStatus.SINGLE,
            contact = ContactRequest(
                email = "AnyName.AnySurname@mail.com",
                phone = "+34-611-222-333"
            )
        )

        assertFailsWith<IllegalArgumentException> {
            transaction {
                employeeRepository.create(employeeRequest = employeeRequest)

                assertEquals(
                    expected = 1,
                    actual = contactRepository.count(),
                    message = "There must be 1 employees in the database."
                )

                // Try to create an employee with an invalid contact detail to make the transaction fail.
                // Done in an even one more nested transaction to test how it behaves.
                // Expected all the transaction tree to be rolled back.
                transaction {
                    assertFailsWith<IllegalArgumentException> {
                        val invalidEmployeeRequest = EmployeeRequest(
                            firstName = "AnyName",
                            lastName = "AnySurname",
                            dob = KLocalDate(year = 2000, monthNumber = 1, dayOfMonth = 1),
                            honorific = Honorific.MR,
                            maritalStatus = MaritalStatus.SINGLE,
                            contact = ContactRequest(
                                email = "X".repeat(100), // Invalid email length..
                                phone = "X".repeat(100), // Invalid phone length.
                            )
                        )

                        employeeRepository.create(employeeRequest = invalidEmployeeRequest)
                    }
                }

                // The transaction will get rollback only after the top-most transaction block is finished.
                assertEquals(
                    expected = 2,
                    actual = employeeRepository.count(),
                    message = "There must be 2 employees in the database."
                )
                assertEquals(
                    expected = 1,
                    actual = contactRepository.count(),
                    message = "There must be 1 contact in the database."
                )

                // Throw an exception to force the rollback.
                throw IllegalArgumentException("Invalid contact detail.")
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
