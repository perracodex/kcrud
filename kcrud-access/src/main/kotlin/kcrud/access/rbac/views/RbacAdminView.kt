/*
 * Copyright (c) 2023-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.access.rbac.views

import kcrud.access.rbac.entity.role.RbacRoleEntity
import kcrud.access.rbac.plugin.annotation.RbacAPI
import kcrud.base.database.schema.admin.rbac.types.RbacAccessLevel
import kcrud.base.database.schema.admin.rbac.types.RbacResource
import kotlinx.html.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.*

@RbacAPI
internal object RbacAdminView {
    const val RBAC_ADMIN_PATH: String = "/rbac/admin"
    const val ROLE_KEY: String = "role"
    const val ROLE_ITEM_KEY: String = "{role_item}"

    @Serializable
    data class AccessLevelKeyData(
        val roleName: String,
        val resource: String,
        val isLocked: Boolean
    )

    fun build(
        html: HTML,
        rbacRoles: List<RbacRoleEntity>,
        currentRoleId: UUID,
        isUpdated: Boolean,
        isViewOnly: Boolean
    ) {
        with(html) {
            head {
                title { +"RBAC Permissions" }
                link(rel = "stylesheet", type = "text/css", href = "/static-rbac/admin.css")
            }
            buildForm(
                rbacRoles = rbacRoles,
                currentRoleId = currentRoleId,
                isUpdated = isUpdated,
                isViewOnly = isViewOnly
            )
        }
    }

    private fun HTML.buildForm(
        rbacRoles: List<RbacRoleEntity>,
        currentRoleId: UUID,
        isUpdated: Boolean,
        isViewOnly: Boolean
    ) {
        body {
            h1 { +"RBAC Permissions" }

            form(action = RBAC_ADMIN_PATH, method = FormMethod.get) {
                showRoleSelector(rbacRoles = rbacRoles, currentRoleId = currentRoleId)
            }

            // If no role is selected, default to the first role.
            val currentRole: RbacRoleEntity = rbacRoles.find { it.id == currentRoleId } ?: rbacRoles.first()
            val isLocked: Boolean = (currentRole.isSuper || isViewOnly)

            form(action = RBAC_ADMIN_PATH, method = FormMethod.post) {
                input(type = InputType.hidden, name = ROLE_KEY) { value = currentRole.id.toString() }

                table {
                    buildTableHeader()
                    buildTableRows(role = currentRole, isLocked = isLocked)
                }

                if (!isLocked) {
                    button(type = ButtonType.submit) { +"Update Permissions" }
                }
            }

            form(action = "/rbac/logout", method = FormMethod.post) {
                button(type = ButtonType.submit) { +"Logout" }
            }

            showUpdateMessage(isUpdated = isUpdated)
        }
    }

    private fun FORM.showRoleSelector(rbacRoles: List<RbacRoleEntity>, currentRoleId: UUID) {
        select {
            id = "roleSelect"
            attributes["name"] = ROLE_KEY
            attributes["onChange"] = "this.form.submit()"

            rbacRoles.forEach { role ->
                val isSelected: Boolean = (role.id == currentRoleId)
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
            th(classes = "resource-column") { +"Resource" }
            th(classes = "access-level-column") { +"Access Level" }
        }
    }

    private fun TABLE.buildTableRows(role: RbacRoleEntity, isLocked: Boolean) {
        RbacResource.entries.forEach { resource ->
            val accessLevel: RbacAccessLevel = role.resourceRules
                .find { it.resource == resource }?.accessLevel ?: RbacAccessLevel.NONE

            buildRowForRoleAndResource(
                roleName = role.roleName,
                resource = resource,
                isLocked = isLocked,
                accessLevel = accessLevel
            )
        }
    }

    private fun TABLE.buildRowForRoleAndResource(
        roleName: String,
        resource: RbacResource,
        isLocked: Boolean,
        accessLevel: RbacAccessLevel
    ) {
        val accessKey: String = ROLE_ITEM_KEY + Json.encodeToString(
            serializer = AccessLevelKeyData.serializer(),
            value = AccessLevelKeyData(
                roleName = roleName,
                resource = resource.name,
                isLocked = isLocked
            )
        )

        tr {
            td(classes = "resource-column") { +resource.name.toCamelCase() }
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
