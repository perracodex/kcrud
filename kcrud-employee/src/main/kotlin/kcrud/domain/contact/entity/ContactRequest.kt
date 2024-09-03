/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.contact.entity

import kcrud.base.persistence.serializers.EmailString
import kotlinx.serialization.Serializable

/**
 * Represents the request to create/update an employee's contact details.
 *
 * The phone and email are verified at service layer level, throwing custom
 * errors when invalid.
 * For the email field, it could alternatively be verified using instead
 * the [EmailString] serializer, but such approach although simpler,
 * would not allow to send a more detailed custom error to the client
 * which could include more contextually relevant information.
 *
 * @property email The contact's email.
 * @property phone The contact's phone number.
 */
@Serializable
public data class ContactRequest(
    val email: String,
    val phone: String
)
