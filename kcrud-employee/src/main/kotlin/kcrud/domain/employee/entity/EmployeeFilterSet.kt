/*
 * Copyright (c) 2024-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.domain.employee.entity

import kcrud.base.database.schema.employee.types.Honorific
import kcrud.base.database.schema.employee.types.MaritalStatus
import kcrud.base.persistence.pagination.Pageable
import kotlinx.serialization.Serializable

/**
 * A set of filters that can be applied to an employee query.
 * All fields are optional, so that the filter can be used with any data combination.
 *
 * @param firstName The first name of the employee.
 * @param lastName The last name of the employee.
 * @param honorific The honorific of the employee.
 * @param maritalStatus The marital status of the employee.
 * @param pageable Pagination settings to apply to the query.
 */
@Serializable
data class EmployeeFilterSet(
    val firstName: String? = null,
    val lastName: String? = null,
    val honorific: List<Honorific>? = null,
    val maritalStatus: List<MaritalStatus>? = null,
    val pageable: Pageable? = null
)
