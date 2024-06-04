/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.server.demo

import io.ktor.server.application.*
import io.ktor.server.auth.*
import kcrud.base.database.schema.employee.types.Honorific
import kcrud.base.database.schema.employee.types.MaritalStatus
import kcrud.base.database.schema.employment.types.EmploymentStatus
import kcrud.base.database.schema.employment.types.WorkModality
import kcrud.base.env.SessionContext
import kcrud.base.persistence.entity.Period
import kcrud.base.utils.KLocalDate
import kcrud.base.utils.TestUtils
import kcrud.domain.contact.entity.ContactRequest
import kcrud.domain.employee.entity.EmployeeEntity
import kcrud.domain.employee.entity.EmployeeRequest
import kcrud.domain.employee.service.EmployeeService
import kcrud.domain.employment.entity.EmploymentRequest
import kcrud.domain.employment.service.EmploymentService
import kotlinx.coroutines.*
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import org.koin.core.parameter.parametersOf
import org.koin.ktor.plugin.scope
import kotlin.random.Random

/**
 * Utility class for demo-related operations.
 */
@DemoAPI
object DemoUtils {

    /**
     * Creates demo records.
     *
     * @param call The application call.
     * @param count The number of records to create.
     */
    suspend fun createDemoRecords(call: ApplicationCall, count: Int) {
        val sessionContext: SessionContext? = call.principal<SessionContext>()
        val employeeService: EmployeeService = call.scope.get<EmployeeService> { parametersOf(sessionContext) }
        val employmentService: EmploymentService = call.scope.get<EmploymentService> { parametersOf(sessionContext) }

        // List to keep track of all the jobs.
        val jobs: MutableList<Deferred<Unit>> = mutableListOf()

        // Launch a coroutine in the I/O dispatcher for each employee creation.
        withContext(Dispatchers.IO) {
            repeat(times = count) {
                // Launch each employee creation as a separate coroutine job.
                val job: Deferred<Unit> = async {
                    val employee: EmployeeEntity = employeeService.create(employeeRequest = newEmployeeRequest())
                    employmentService.create(
                        employeeId = employee.id,
                        employmentRequest = newEmploymentRequest(employee = employee)
                    )
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

    private fun newEmploymentRequest(employee: EmployeeEntity): EmploymentRequest {
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