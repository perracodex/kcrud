/*
 * Copyright (c) 2024-Present Perracodex. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.domain.contact.entity

import kcrud.base.database.custom_columns.validVarChar
import kcrud.base.persistence.serializers.EmailString
import kcrud.domain.employee.errors.EmployeeError
import kotlinx.serialization.Serializable

/**
 * Represents the request to create/update an employee's contact details.
 *
 * This entity serves as example of how to use the [EmailString],
 * which is a typealias for a serializable String that must be a valid email.
 *
 * Note that the project also includes examples demonstrating how perform verifications
 * at service level or database field level, instead of using serializers.
 * Such validation variants can send to the client a more detailed error than
 * would do a serializer. See: [EmployeeError], [validVarChar]
 *
 * @property email The contact's email. Must be a valid email.
 * @property phone The contact's phone. Must be a valid phone number.
 */
@Serializable
data class ContactRequest(
    val email: EmailString,
    val phone: String
)
