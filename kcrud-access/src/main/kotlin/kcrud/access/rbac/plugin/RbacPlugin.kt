/*
 * Copyright (c) 2023-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.access.rbac.plugin

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import kcrud.access.rbac.plugin.annotation.RbacAPI
import kcrud.access.rbac.service.RbacService
import kcrud.access.system.SessionContext
import kcrud.base.database.schema.admin.rbac.types.RbacAccessLevel
import kcrud.base.database.schema.admin.rbac.types.RbacResource
import org.koin.ktor.ext.inject

/**
 * Configuration for the RBAC plugin.
 * Holds the RBAC target resource and the required access level.
 */
@RbacAPI
internal class RbacPluginConfig {
    /**
     * The RBAC resource associated with the route, defining the scope of access control.
     */
    lateinit var resource: RbacResource

    /**
     * The RBAC access level required for accessing the route, defining the degree of access control.
     */
    lateinit var accessLevel: RbacAccessLevel
}

/**
 * Custom Ktor RBAC plugin intercepting calls to routes, and
 * applying RBAC checks based on the configured resource and access level.
 *
 * It ensures that only authorized Actors, as per the RBAC settings,
 * can access specific routes.
 */
@RbacAPI
internal val RbacPlugin = createRouteScopedPlugin(
    name = "RbacPlugin",
    createConfiguration = ::RbacPluginConfig
) {
    on(hook = AuthenticationChecked) { call ->

        val sessionContext: SessionContext? = call.principal<SessionContext>()
            ?: call.sessions.get(name = SessionContext.SESSION_NAME) as SessionContext?

        sessionContext?.let {
            val rbacService: RbacService by call.application.inject()
            val rbacResource: RbacResource = pluginConfig.resource
            val rbacAccessLevel: RbacAccessLevel = pluginConfig.accessLevel

            val hasPermission: Boolean = rbacService.hasPermission(
                sessionContext = it,
                resource = rbacResource,
                accessLevel = rbacAccessLevel
            )

            if (hasPermission) {
                // The call is authorized to proceed.
                return@on
            }
        }

        call.respond(status = HttpStatusCode.Forbidden, message = "Access denied.")
    }
}
