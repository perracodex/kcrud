/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.domain.employee.errors

import io.ktor.http.*
import kcrud.base.infrastructure.errors.BaseError
import kcrud.base.infrastructure.errors.ErrorCodeRegistry
import java.util.*

sealed class EmployeeError(
    status: HttpStatusCode,
    code: String,
    description: String
) : BaseError(status = status, code = code, description = description) {

    data class EmployeeNotFound(val employeeId: UUID) : EmployeeError(
        status = HttpStatusCode.NotFound,
        code = "${TAG}ENF",
        description = "Employee not found. Employee Id: $employeeId"
    )

    data class InvalidEmailFormat(val employeeId: UUID?, val email: String) : EmployeeError(
        status = HttpStatusCode.BadRequest,
        code = "${TAG}IEF",
        description = "Invalid email format: '$email'. Employee Id: $employeeId"
    )

    data class InvalidPhoneFormat(val employeeId: UUID?, val phone: String) : EmployeeError(
        status = HttpStatusCode.BadRequest,
        code = "${TAG}IPF",
        description = "Invalid phone format: '$phone'. Employee Id: $employeeId"
    )

    companion object {
        private const val TAG: String = "EMP."

        init {
            ErrorCodeRegistry.registerTag(tag = TAG)
        }
    }
}
