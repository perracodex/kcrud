/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.rbac.routing.admin

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.access.rbac.entity.role.RbacRoleEntity
import kcrud.access.rbac.plugin.annotation.RbacAPI
import kcrud.access.rbac.routing.login.getRbacAdminAccessActor
import kcrud.access.rbac.service.RbacService
import kcrud.access.rbac.view.RbacAdminView
import kcrud.access.rbac.view.RbacLoginView
import kcrud.base.database.schema.admin.rbac.types.RbacAccessLevel
import kcrud.base.database.schema.admin.rbac.types.RbacScope
import kcrud.base.env.SessionContext
import kcrud.base.persistence.utils.toUuidOrNull
import kotlin.uuid.Uuid

/**
 * Handles the GET request for the RBAC admin panel.
 */
@RbacAPI
internal fun Route.rbacAdminRouteGet(rbacService: RbacService) {
    get("rbac/admin") {
        val sessionContext: SessionContext? = getRbacAdminAccessActor(call = call)
        if (sessionContext == null) {
            call.respondRedirect(url = RbacLoginView.RBAC_LOGIN_PATH)
            return@get
        }

        val rbacAccessLevel: RbacAccessLevel = rbacService.getPermissionLevel(
            sessionContext = sessionContext,
            scope = RbacScope.RBAC_ADMIN
        )

        val isViewOnly: Boolean = (rbacAccessLevel == RbacAccessLevel.VIEW)
        val selectedRoleId: Uuid? = call.parameters[RbacAdminView.ROLE_KEY].toUuidOrNull()
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
