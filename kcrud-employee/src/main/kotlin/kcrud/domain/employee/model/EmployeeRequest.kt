/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employee.model

import kcrud.core.database.schema.employee.type.Honorific
import kcrud.core.database.schema.employee.type.MaritalStatus
import kcrud.core.persistence.serializer.NoBlankString
import kcrud.domain.contact.model.ContactRequest
import kcrud.domain.employee.error.EmployeeError
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * Represents the request to create/update an employee.
 *
 * This request serves as example of how to use the [NoBlankString],
 * which is a typealias for a serializable String that cannot be blank.
 *
 * Note that the project also includes examples demonstrating how perform verifications
 * at service level or database field level, instead of using serializers.
 * Such validation variants can send to the client a more detailed error than
 * would do a serializer.
 *
 * @property firstName The first name of the employee. Must not be blank.
 * @property lastName The last name of the employee. Must not be blank.
 * @property workEmail The unique work email of the employee. Must not be blank.
 * @property dob The date of birth of the employee.
 * @property maritalStatus The [MaritalStatus] of the employee.
 * @property honorific The [Honorific] or title of the employee.
 * @property contact Optional [ContactRequest] details of the employee.
 *
 * @see [EmployeeError]
 */
@Serializable
public data class EmployeeRequest(
    val firstName: NoBlankString,
    val lastName: NoBlankString,
    val workEmail: NoBlankString,
    val dob: LocalDate,
    val maritalStatus: MaritalStatus,
    val honorific: Honorific,
    val contact: ContactRequest? = null
)
