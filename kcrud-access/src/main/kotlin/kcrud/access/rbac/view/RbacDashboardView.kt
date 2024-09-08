/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.rbac.view

import kcrud.access.rbac.entity.role.RbacRoleEntity
import kcrud.access.rbac.plugin.annotation.RbacAPI
import kcrud.access.rbac.service.RbacDashboardManager
import kcrud.base.database.schema.admin.rbac.types.RbacAccessLevel
import kcrud.base.database.schema.admin.rbac.types.RbacScope
import kotlinx.html.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@RbacAPI
internal object RbacDashboardView {
    const val RBAC_DASHBOARD_PATH: String = "/rbac/dashboard"
    const val ROLE_KEY: String = "role"
    const val ROLE_ITEM_KEY: String = "{role_item}"

    @Serializable
    data class AccessLevelKeyData(val roleName: String, val scope: String, val isLocked: Boolean)

    fun build(
        html: HTML,
        isUpdated: Boolean,
        dashboardContext: RbacDashboardManager.Context
    ) {
        with(html) {
            head {
                title { +"RBAC Permissions" }
                link(rel = "stylesheet", type = "text/css", href = "/static-rbac/dashboard.css")
            }
            buildForm(
                isUpdated = isUpdated,
                dashboardContext = dashboardContext
            )
        }
    }

    private fun HTML.buildForm(
        isUpdated: Boolean,
        dashboardContext: RbacDashboardManager.Context
    ) {
        body {
            h1 { +"RBAC Permissions" }
            h3 { +"Logged-In Role: ${dashboardContext.sessionRoleName}" }

            form(action = RBAC_DASHBOARD_PATH, method = FormMethod.get) {
                showRoleSelector(dashboardContext = dashboardContext)
            }

            // If no role is selected, default to the first role.
            val currentRole: RbacRoleEntity = dashboardContext.rbacRoles.find { role ->
                role.id == dashboardContext.targetRole.id
            } ?: dashboardContext.rbacRoles.first()

            form(action = RBAC_DASHBOARD_PATH, method = FormMethod.post) {
                input(type = InputType.hidden, name = ROLE_KEY) { value = currentRole.id.toString() }

                table {
                    buildTableHeader()
                    buildTableRows(dashboardContext = dashboardContext)
                }

                if (!dashboardContext.isViewOnly) {
                    button(type = ButtonType.submit) { +"Update Permissions" }
                }
            }

            form(action = "/rbac/logout", method = FormMethod.post) {
                button(type = ButtonType.submit) { +"Logout" }
            }

            showUpdateMessage(isUpdated = isUpdated)
        }
    }

    private fun FORM.showRoleSelector(dashboardContext: RbacDashboardManager.Context) {
        div(classes = "role-selector") {
            label {
                htmlFor = "roleSelect"
                +"Edit Role:"
            }
            select {
                id = "roleSelect"
                attributes["name"] = ROLE_KEY
                attributes["onChange"] = "this.form.submit()"

                dashboardContext.rbacRoles.forEach { role ->
                    val isSelected: Boolean = (role.id == dashboardContext.targetRole.id)
                    option {
                        value = role.id.toString()
                        if (isSelected) {
                            attributes["selected"] = "selected"
                        }
                        +role.roleName
                    }
                }
            }
        }
    }

    private fun BODY.showUpdateMessage(isUpdated: Boolean) {
        // Conditional block to include the message if data has been updated.
        if (isUpdated) {
            div {
                style = "display: flex; justify-content: center; align-items: center; height: 50px;"
                div {
                    id = "updateMessage"
                    style = "background-color: #547597; color: white; padding: 10px; border-radius: 5px;"
                    +"Update Successful"
                }
            }
        }

        // JavaScript for message display and timeout.
        script(type = ScriptType.textJavaScript) {
            unsafe {
                +"""
                    window.onload = function() {
                        var message = document.getElementById("updateMessage");
                        if (message) {
                            setTimeout(function() { message.style.display = 'none'; }, 2000);
                        }
                    }
                    """
            }
        }
    }

    private fun TABLE.buildTableHeader() {
        tr {
            th(classes = "scope-column") { +"Scope" }
            th(classes = "access-level-column") { +"Access Level" }
        }
    }

    private fun TABLE.buildTableRows(dashboardContext: RbacDashboardManager.Context) {
        RbacScope.entries.forEach { scope ->
            val accessLevel: RbacAccessLevel = dashboardContext.targetRole.scopeRules.find { scopeRule ->
                scopeRule.scope == scope
            }?.accessLevel ?: RbacAccessLevel.NONE

            buildRowForRoleAndScope(
                roleName = dashboardContext.targetRole.roleName,
                scope = scope,
                isLocked = dashboardContext.isViewOnly,
                accessLevel = accessLevel
            )
        }
    }

    private fun TABLE.buildRowForRoleAndScope(
        roleName: String,
        scope: RbacScope,
        isLocked: Boolean,
        accessLevel: RbacAccessLevel
    ) {
        val accessKey: String = ROLE_ITEM_KEY + Json.encodeToString(
            serializer = AccessLevelKeyData.serializer(),
            value = AccessLevelKeyData(
                roleName = roleName,
                scope = scope.name,
                isLocked = isLocked
            )
        )

        tr {
            td(classes = "scope-column") { +scope.name.toCamelCase() }
            buildAccessLevelDropdown(key = accessKey, isLocked = isLocked, currentLevel = accessLevel)
        }
    }

    private fun String.toCamelCase() =
        split('_').joinToString(" ") {
            it.lowercase().replaceFirstChar { char ->
                char.uppercase()
            }
        }

    private fun TR.buildAccessLevelDropdown(key: String, isLocked: Boolean, currentLevel: RbacAccessLevel) {
        td(classes = "access-level-column") {
            if (isLocked) {
                +currentLevel.name
                input(type = InputType.hidden, name = key) { value = currentLevel.name }
            } else {
                select {
                    attributes["name"] = key
                    RbacAccessLevel.entries.forEach { accessLevel ->
                        option {
                            attributes["value"] = accessLevel.name
                            if (accessLevel == currentLevel) attributes["selected"] = "selected"
                            +accessLevel.name
                        }
                    }
                }
            }
        }
    }
}
