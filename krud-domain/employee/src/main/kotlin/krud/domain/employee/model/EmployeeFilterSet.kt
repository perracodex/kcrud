/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package krud.domain.employee.model

import kotlinx.serialization.Serializable
import krud.database.schema.employee.type.Honorific
import krud.database.schema.employee.type.MaritalStatus

/**
 * Represents a set of optional filters for querying employees.
 *
 * Each non-null field in this filter set is combined using logical AND conditions.
 * Only employees matching **all** specified conditions will be returned.
 *
 * @property firstName The first name of the employee to filter by.
 * @property lastName The last name of the employee to filter by.
 * @property workEmail The work email of the employee to filter by.
 * @property contactEmail The contact email of the employee to filter by.
 * @property honorific A [Honorific] list choices to filter the employees.
 * @property maritalStatus A list of [MaritalStatus] choices to filter the employees.
 */
@Serializable
public data class EmployeeFilterSet private constructor(
    val firstName: String? = null,
    val lastName: String? = null,
    val workEmail: String? = null,
    val contactEmail: String? = null,
    val honorific: List<Honorific>? = null,
    val maritalStatus: List<MaritalStatus>? = null
)
