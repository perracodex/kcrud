/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.server.demo

import kcrud.core.database.schema.employee.types.Honorific
import kcrud.core.database.schema.employee.types.MaritalStatus
import kcrud.core.database.schema.employment.types.EmploymentStatus
import kcrud.core.database.schema.employment.types.WorkModality
import kcrud.core.persistence.model.Period
import kcrud.core.utils.KLocalDate
import kcrud.core.utils.TestUtils
import kcrud.domain.contact.model.ContactRequest
import kcrud.domain.employee.model.Employee
import kcrud.domain.employee.model.EmployeeRequest
import kcrud.domain.employee.service.EmployeeService
import kcrud.domain.employment.model.EmploymentRequest
import kcrud.domain.employment.service.EmploymentService
import kotlinx.coroutines.*
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import kotlin.random.Random

/**
 * Utility class for demo-related operations.
 */
@DemoAPI
internal object DemoUtils {

    /**
     * Creates demo records.
     *
     * @param employeeService A reference to the [EmployeeService].
     * @param employmentService A reference to the [EmploymentService].
     * @param count The number of records to create.
     */
    suspend fun createDemoRecords(
        employeeService: EmployeeService,
        employmentService: EmploymentService,
        count: Int
    ) {
        // List to keep track of all the jobs.
        val jobs: MutableList<Deferred<Unit>> = mutableListOf()

        // Launch a coroutine in the I/O dispatcher for each employee creation.
        withContext(Dispatchers.IO) {
            repeat(times = count) {
                // Launch each employee creation as a separate coroutine job.
                val job: Deferred<Unit> = async {
                    val result: Result<Employee> = employeeService.create(request = newEmployeeRequest())
                    val employee: Employee = result.getOrThrow()
                    employmentService.create(
                        employeeId = employee.id,
                        request = newEmploymentRequest(employee = employee)
                    ).getOrThrow()
                }
                jobs.add(job)
            }

            // Wait for all jobs to complete.
            jobs.awaitAll()
        }
    }

    private fun newEmployeeRequest(): EmployeeRequest {
        val firstName: String = TestUtils.randomName()
        val lastName: String = TestUtils.randomName()

        return EmployeeRequest(
            firstName = firstName,
            lastName = lastName,
            dob = TestUtils.randomDob(),
            honorific = Honorific.entries.random(),
            maritalStatus = MaritalStatus.entries.random(),
            contact = ContactRequest(
                email = "$firstName.$lastName@kcrud.com".lowercase(),
                phone = TestUtils.randomPhoneNumber()
            )
        )
    }

    private fun newEmploymentRequest(employee: Employee): EmploymentRequest {
        val period: Period = TestUtils.randomPeriod(threshold = employee.dob)
        val probationEndDate: KLocalDate = period.startDate.plus(
            value = Random.nextInt(from = 3, until = 6),
            unit = DateTimeUnit.MONTH
        )

        // If active, then 80% chance of being active, otherwise onboarding.
        val status = when (period.isActive) {
            true -> if (Random.nextInt(from = 0, until = 100) < 80) {
                EmploymentStatus.ACTIVE
            } else {
                EmploymentStatus.ONBOARDING
            }

            false -> EmploymentStatus.TERMINATED
        }

        return EmploymentRequest(
            period = period,
            probationEndDate = probationEndDate,
            status = status,
            workModality = WorkModality.entries.random()
        )
    }
}
