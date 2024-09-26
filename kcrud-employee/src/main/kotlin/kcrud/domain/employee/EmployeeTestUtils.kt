/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employee

import kcrud.core.database.schema.employee.types.Honorific
import kcrud.core.database.schema.employee.types.MaritalStatus
import kcrud.core.security.snowflake.SnowflakeFactory
import kcrud.core.utils.TestUtils
import kcrud.domain.contact.model.ContactRequest
import kcrud.domain.employee.model.EmployeeRequest

/**
 * Utility class for test-related operations.
 */
public object EmployeeTestUtils {
    /**
     * Creates a new [EmployeeRequest] with random values.
     */
    public fun newEmployeeRequest(): EmployeeRequest {
        val firstName: String = TestUtils.randomName()
        val lastName: String = TestUtils.randomName()
        val snowflakeId: String = SnowflakeFactory.nextId()

        return EmployeeRequest(
            firstName = firstName,
            lastName = lastName,
            workEmail = "$firstName.$lastName.$snowflakeId@work.com",
            dob = TestUtils.randomDob(),
            honorific = Honorific.entries.random(),
            maritalStatus = MaritalStatus.entries.random(),
            contact = ContactRequest(
                email = "$lastName.$firstName@public.com",
                phone = TestUtils.randomPhoneNumber()
            )
        )
    }
}