/*
 * Copyright (c) 2023-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.admin.rbac.routing.admin

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.base.admin.rbac.entities.role.RbacRoleEntity
import kcrud.base.admin.rbac.plugin.annotation.RbacAPI
import kcrud.base.admin.rbac.plugin.withRbac
import kcrud.base.admin.rbac.routing.login.getRbacAdminAccessActor
import kcrud.base.admin.rbac.service.RbacService
import kcrud.base.admin.rbac.views.RbacAdminView
import kcrud.base.admin.rbac.views.RbacLoginView
import kcrud.base.database.schema.admin.rbac.types.RbacAccessLevel
import kcrud.base.database.schema.admin.rbac.types.RbacResource
import kcrud.base.infrastructure.env.SessionContext
import org.koin.ktor.ext.inject
import java.util.*

@OptIn(RbacAPI::class)
fun Route.rbacAdminRoute() {
    val rbacService: RbacService by inject()

    withRbac(resource = RbacResource.RBAC_ADMIN, accessLevel = RbacAccessLevel.VIEW) {
        rbacResourceRulesRoute(rbacService = rbacService)
        rbacResourceRulesProcessingRoute(rbacService = rbacService)
    }
}

@RbacAPI
private fun Route.rbacResourceRulesRoute(rbacService: RbacService) {
    get("rbac/admin") {
        val sessionContext: SessionContext? = getRbacAdminAccessActor(call = call)
        if (sessionContext == null) {
            call.respondRedirect(url = RbacLoginView.RBAC_LOGIN_PATH)
            return@get
        }

        val rbacAccessLevel: RbacAccessLevel = rbacService.getPermissionLevel(
            sessionContext = sessionContext,
            resource = RbacResource.RBAC_ADMIN
        )

        val isViewOnly: Boolean = (rbacAccessLevel == RbacAccessLevel.VIEW)
        val selectedRoleId: UUID? = call.parameters[RbacAdminView.ROLE_KEY]?.let(UUID::fromString)
        val rbacRoles: List<RbacRoleEntity> = rbacService.findAllRoles()

        // If no role is selected, default to the first role.
        val currentRoleId: RbacRoleEntity = selectedRoleId?.let { roleId ->
            rbacRoles.find { it.id == roleId }
        } ?: rbacRoles.first()

        call.respondHtml(status = HttpStatusCode.OK) {
            RbacAdminView.build(
                html = this,
                rbacRoles = rbacRoles,
                currentRoleId = currentRoleId.id,
                isUpdated = false,
                isViewOnly = isViewOnly
            )
        }
    }
}
