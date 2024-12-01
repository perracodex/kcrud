/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employee.test

import kcrud.core.security.snowflake.SnowflakeFactory
import kcrud.database.schema.employee.type.Honorific
import kcrud.database.schema.employee.type.MaritalStatus
import kcrud.database.test.DatabaseTestUtils
import kcrud.domain.contact.model.ContactRequest
import kcrud.domain.employee.model.EmployeeRequest

/**
 * Common utilities for Employee unit testing.
 */
public object EmployeeTestUtils {
    /**
     * Creates a new [EmployeeRequest] with random values.
     */
    public fun newEmployeeRequest(): EmployeeRequest {
        val firstName: String = DatabaseTestUtils.randomName()
        val lastName: String = DatabaseTestUtils.randomName()
        val snowflakeId: String = SnowflakeFactory.nextId()

        return EmployeeRequest(
            firstName = firstName,
            lastName = lastName,
            workEmail = "$lastName.$snowflakeId@work.com",
            dob = DatabaseTestUtils.randomDob(),
            honorific = Honorific.entries.random(),
            maritalStatus = MaritalStatus.entries.random(),
            contact = ContactRequest(
                email = "$lastName.$firstName@public.com",
                phone = DatabaseTestUtils.randomPhoneNumber()
            )
        )
    }
}
