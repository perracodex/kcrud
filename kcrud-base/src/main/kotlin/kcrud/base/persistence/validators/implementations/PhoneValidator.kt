/*
 * Copyright (c) 2023-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.persistence.validators.implementations

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import kcrud.base.infrastructure.utils.Tracer
import kcrud.base.persistence.validators.IValidator

/**
 * Verifies if a phone number is in the correct format.
 */
object PhoneValidator : IValidator {
    private val tracer = Tracer<PhoneValidator>()

    override fun validate(value: String): IValidator.Result {

        try {
            val phoneUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()

            // Region code is null for international numbers.
            val numberProto: Phonenumber.PhoneNumber? = phoneUtil.parse(value, null)

            if (!phoneUtil.isValidNumber(numberProto)) {
                return IValidator.Result.Failure(reason = "Invalid phone number. $value")
            }
        } catch (e: NumberParseException) {
            tracer.error("Error parsing phone number: $value", e)
            return IValidator.Result.Failure(reason = e.message ?: "Error parsing phone number. $value")
        }

        return IValidator.Result.Success
    }

    override fun message(text: String): String {
        return "Invalid phone number: $text"
    }
}
