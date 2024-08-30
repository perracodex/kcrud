/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.rbac.service

import io.ktor.server.application.*
import io.ktor.server.sessions.*
import kcrud.access.rbac.entity.role.RbacRoleEntity
import kcrud.access.rbac.entity.scope.RbacScopeRuleRequest
import kcrud.access.rbac.plugin.annotation.RbacAPI
import kcrud.access.rbac.view.RbacAdminView
import kcrud.base.database.schema.admin.rbac.types.RbacAccessLevel
import kcrud.base.database.schema.admin.rbac.types.RbacScope
import kcrud.base.env.SessionContext
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.java.KoinJavaComponent
import kotlin.uuid.Uuid

/**
 * Manages access control, role determination, and updates for the RBAC admin panel,
 * including permission checks, role-specific access level determination,
 * and processing of administrative updates.
 */
@RbacAPI
internal object RbacAdminPanelManager : KoinComponent {

    /**
     * Get the [SessionContext] from the given [ApplicationCall]
     * if the user has access to the RBAC admin panel.
     *
     * @param call The application call.
     * @return The [SessionContext] if the user has access to the RBAC admin panel; null otherwise.
     */
    suspend fun getSessionContext(call: ApplicationCall): SessionContext? {
        val sessionContext: SessionContext? = call.sessions.get<SessionContext>()

        return sessionContext?.takeIf {
            val rbacService: RbacService = KoinJavaComponent.getKoin().get()
            rbacService.hasPermission(
                sessionContext = it,
                scope = RbacScope.RBAC_ADMIN,
                accessLevel = RbacAccessLevel.VIEW
            )
        }
    }

    /**
     * Determines the current user's role and access level within the
     * RBAC admin scope based on the provided session context and role ID.
     *
     * @param sessionContext The current user's session context.
     * @param roleId The role ID if specified, otherwise null.
     * @return [AccessDetails] containing the access details for the RBAC admin panel.
     */
    suspend fun determineAccessDetails(sessionContext: SessionContext, roleId: Uuid?): AccessDetails {
        val rbacService: RbacService by inject()

        // Fetch the current user's permission level for the RBAC admin scope.
        val rbacAccessLevel: RbacAccessLevel = rbacService.getPermissionLevel(
            sessionContext = sessionContext,
            scope = RbacScope.RBAC_ADMIN
        )

        // Determine the view-only status,  and retrieve all RBAC roles.
        val isViewOnly: Boolean = (rbacAccessLevel == RbacAccessLevel.VIEW)
        val rbacRoles: List<RbacRoleEntity> = rbacService.findAllRoles()

        // Select the current role based on the role ID or default to the first role if none specified.
        val currentRole: RbacRoleEntity = roleId?.let { id ->
            rbacRoles.find { it.id == id }
        } ?: rbacRoles.first()

        return AccessDetails(isViewOnly = isViewOnly, rbacRoles = rbacRoles, currentRole = currentRole)
    }

    /**
     * Process updates to the RBAC admin panel.
     *
     * @param sessionContext The current user's session context.
     * @param roleId The role ID to update.
     * @param updates The updates to apply to the role.
     * @return The result of the update operation.
     */
    suspend fun processAdminUpdate(sessionContext: SessionContext, roleId: Uuid, updates: Map<String, String>): UpdateResult {
        // Update role-specific scope rules based on submitted parameters.
        processUpdates(roleId = roleId, updates = updates)

        // Check if the user has permission to access the RBAC admin panel.
        val rbacService: RbacService by inject()
        val permissionLevel: RbacAccessLevel = rbacService.getPermissionLevel(sessionContext, RbacScope.RBAC_ADMIN)
        if (permissionLevel == RbacAccessLevel.NONE) {
            return UpdateResult.Unauthorized
        }

        // Fetch updated roles for immediate feedback on UI.
        val rbacRoles: List<RbacRoleEntity> = rbacService.findAllRoles()
        val isViewOnly: Boolean = permissionLevel == RbacAccessLevel.VIEW

        // Return the updated roles and current role ID.
        return UpdateResult.Success(roles = rbacRoles, currentRoleId = roleId, isViewOnly = isViewOnly)
    }

    /**
     * Process updates to the RBAC settings for a specific role.
     *
     * @param roleId The role ID to update.
     * @param updates The updates to apply to the role.
     */
    private suspend fun processUpdates(roleId: Uuid, updates: Map<String, String>) {
        // Collect role-specific access rules from parameters.
        val scopeRulesRequests: MutableList<RbacScopeRuleRequest> = mutableListOf()

        // Decode and prepare new access rules from received parameters.
        updates.filter { it.key.startsWith(prefix = RbacAdminView.ROLE_ITEM_KEY) }
            .map {
                it to Json.decodeFromString<RbacAdminView.AccessLevelKeyData>(
                    it.key.removePrefix(prefix = RbacAdminView.ROLE_ITEM_KEY)
                )
            }
            .filter { (_, accessKey) -> !accessKey.isLocked }
            .forEach { (paramEntry, accessKey) ->
                val accessLevel: RbacAccessLevel = RbacAccessLevel.valueOf(paramEntry.value)

                // Add valid scope rule requests for update.
                if (accessLevel != RbacAccessLevel.NONE) {
                    val scopeRuleRequest = RbacScopeRuleRequest(
                        scope = RbacScope.valueOf(accessKey.scope),
                        accessLevel = accessLevel
                    )
                    scopeRulesRequests.add(scopeRuleRequest)
                }
            }

        // Update role with new access rules.
        val rbacService: RbacService by inject()
        rbacService.updateScopeRules(
            roleId = roleId,
            scopeRuleRequests = scopeRulesRequests.toList()
        )
    }

    /**
     * Sealed class to represent the possible outcomes of an RBAC admin panel update.
     */
    sealed class UpdateResult {
        /**
         * Represents a successful update operation.
         *
         * @param roles The updated list of RBAC roles.
         * @param currentRoleId The current role ID.
         * @param isViewOnly The view-only status for the RBAC admin panel.
         */
        data class Success(val roles: List<RbacRoleEntity>, val currentRoleId: Uuid, val isViewOnly: Boolean) : UpdateResult()

        /**
         * Represents a failed update operation due to unauthorized access.
         */
        data object Unauthorized : UpdateResult()
    }

    /**
     * Data class to hold the details necessary for rendering the RBAC admin panel.
     *
     * @param isViewOnly The view-only status for the RBAC admin panel.
     * @param rbacRoles The list of RBAC roles.
     * @param currentRole The current role selected by the user.
     */
    data class AccessDetails(
        val isViewOnly: Boolean,
        val rbacRoles: List<RbacRoleEntity>,
        val currentRole: RbacRoleEntity
    )
}
