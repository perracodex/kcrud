/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package krud.domain.employment.service

import krud.base.error.AppException
import krud.base.error.CompositeAppException
import krud.base.util.getPropertyName
import krud.database.model.Period
import krud.domain.employment.error.EmploymentError
import krud.domain.employment.model.EmploymentRequest
import kotlin.uuid.Uuid

/**
 * Provides verification methods for employment-related operations.
 */
internal object EmploymentConstraints {
    /**
     * Verifies if the integrity of the given [request] is valid.
     *
     * @param employeeId The ID of the employee associated with the employment.
     * @param employmentId The ID of the employment to be verified.
     * @param request The [EmploymentRequest] details to be verified.
     * @param reason The reason for the verification. To be included in the error messages.
     * @return A [Result] with verification state.
     */
    fun verify(
        employeeId: Uuid,
        employmentId: Uuid?,
        request: EmploymentRequest,
        reason: String
    ): Result<Unit> {
        val errors: MutableList<AppException> = mutableListOf()

        // Verify that the employment period dates are valid.
        request.period.endDate?.let { endDate ->
            if (endDate < request.period.startDate) {
                errors.add(
                    EmploymentError.PeriodDatesMismatch(
                        employeeId = employeeId,
                        employmentId = employmentId,
                        startDate = request.period.startDate,
                        endDate = endDate,
                        field = getPropertyName(root = EmploymentRequest::period, Period::endDate),
                        reason = reason
                    )
                )
            }
        }

        // Verify that the employment probation end date is valid.
        request.probationEndDate?.let { probationEndDate ->
            if (probationEndDate < request.period.startDate) {
                errors.add(
                    EmploymentError.InvalidProbationEndDate(
                        employeeId = employeeId,
                        employmentId = employmentId,
                        startDate = request.period.startDate,
                        probationEndDate = probationEndDate,
                        field = getPropertyName(root = EmploymentRequest::probationEndDate),
                        reason = reason
                    )
                )
            }
        }

        if (errors.isNotEmpty()) {
            return Result.failure(CompositeAppException(errors))
        }

        return Result.success(Unit)
    }
}
