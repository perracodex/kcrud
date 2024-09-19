/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.errors

import io.ktor.http.*
import kcrud.base.errors.AppException
import kotlin.uuid.Uuid

/**
 * Concrete errors for the RBAC domain.
 *
 * @param statusCode The [HttpStatusCode] associated with this error.
 * @param errorCode A unique code identifying the type of error.
 * @param description A human-readable description of the error.
 * @param reason An optional human-readable reason for the exception, providing more context.
 * @param cause The underlying cause of the exception, if any.
 */
internal sealed class RbacError(
    statusCode: HttpStatusCode,
    errorCode: String,
    description: String,
    reason: String? = null,
    cause: Throwable? = null
) : AppException(
    statusCode = statusCode,
    errorCode = errorCode,
    context = "RBAC",
    description = description,
    reason = reason,
    error = cause
) {
    /**
     * Error for when an actor has no roles.
     * All actors must have at least one role,
     * so this is considered an internal server error.
     *
     * @param actorId The ID of the actor with no roles.
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
