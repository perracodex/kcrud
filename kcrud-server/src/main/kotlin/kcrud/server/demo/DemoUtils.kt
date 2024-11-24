/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.server.demo

import kcrud.core.database.schema.employment.type.EmploymentStatus
import kcrud.core.database.schema.employment.type.WorkModality
import kcrud.core.persistence.model.Period
import kcrud.core.test.TestUtils
import kcrud.domain.employee.model.Employee
import kcrud.domain.employee.model.EmployeeRequest
import kcrud.domain.employee.service.EmployeeService
import kcrud.domain.employee.test.EmployeeTestUtils
import kcrud.domain.employment.model.EmploymentRequest
import kcrud.domain.employment.service.EmploymentService
import kotlinx.coroutines.*
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlin.random.Random

/**
 * Utility class for demo-related operations.
 */
@DemoApi
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
                    val employeeRequest: EmployeeRequest = EmployeeTestUtils.newEmployeeRequest()
                    val result: Result<Employee> = employeeService.create(request = employeeRequest)
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

    private fun newEmploymentRequest(employee: Employee): EmploymentRequest {
        val period: Period = TestUtils.randomPeriod(threshold = employee.dob)
        val probationEndDate: LocalDate = period.startDate.plus(
            value = Random.nextInt(from = 3, until = 6),
            unit = DateTimeUnit.MONTH
        )

        // If active, then 80% chance of being active, otherwise onboarding.
        @Suppress("MagicNumber")
        val status: EmploymentStatus = when (period.isActive) {
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
            workModality = WorkModality.entries.random(),
            sensitiveData = "Sensitive data: ${System.nanoTime()}"
        )
    }
}
