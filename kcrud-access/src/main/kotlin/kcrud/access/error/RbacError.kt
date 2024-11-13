/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.error

import io.ktor.http.*
import kcrud.core.error.AppException
import kotlin.uuid.Uuid

/**
 * Concrete errors for the RBAC domain.
 *
 * @param statusCode The [HttpStatusCode] associated with this error.
 * @param errorCode A unique code identifying the type of error.
 * @param description A human-readable description of the error.
 * @param field Optional field identifier, typically the input field that caused the error.
 * @param reason An optional human-readable reason for the exception, providing more context.
 * @param cause The underlying cause of the exception, if any.
 */
internal sealed class RbacError(
    statusCode: HttpStatusCode,
    errorCode: String,
    description: String,
    field: String? = null,
    reason: String? = null,
    cause: Throwable? = null
) : AppException(
    statusCode = statusCode,
    errorCode = errorCode,
    context = "RBAC",
    description = description,
    field = field,
    reason = reason,
    cause = cause
) {
    /**
     * Error for when an actor has no roles.
     * All actors must have at least one role,
     * so this is considered an internal server error.
     *
     * @param actorId The ID of the actor with no roles.
     * @param reason Optional human-readable reason for the exception, providing more context.
     * @param cause Optional underlying cause of the exception, if any.
     */
    class ActorWithNoRoles(
        actorId: Uuid,
        reason: String? = null,
        cause: Throwable? = null
    ) : RbacError(
        statusCode = STATUS_CODE,
        errorCode = ERROR_CODE,
        description = "No roles found for actor with ID: $actorId",
        reason = reason,
        cause = cause
    ) {
        companion object {
            val STATUS_CODE: HttpStatusCode = HttpStatusCode.InternalServerError
            const val ERROR_CODE: String = "ACTOR_WITH_NO_ROLES"
        }
    }
}
