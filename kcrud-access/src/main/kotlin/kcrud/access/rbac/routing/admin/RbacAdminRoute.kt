/*
 * Copyright (c) 2023-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.access.rbac.routing.admin

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.access.rbac.entity.role.RbacRoleEntity
import kcrud.access.rbac.plugin.annotation.RbacAPI
import kcrud.access.rbac.plugin.withRbac
import kcrud.access.rbac.routing.login.getRbacAdminAccessActor
import kcrud.access.rbac.service.RbacService
import kcrud.access.rbac.views.RbacAdminView
import kcrud.access.rbac.views.RbacLoginView
import kcrud.base.database.schema.admin.rbac.types.RbacAccessLevel
import kcrud.base.database.schema.admin.rbac.types.RbacResource
import kcrud.base.env.SessionContext
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
