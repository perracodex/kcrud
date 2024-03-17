/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.plugins

import io.ktor.server.application.*
import kcrud.base.database.plugin.DbPlugin
import kcrud.base.database.schema.admin.actor.ActorTable
import kcrud.base.database.schema.admin.rbac.RbacFieldRuleTable
import kcrud.base.database.schema.admin.rbac.RbacResourceRuleTable
import kcrud.base.database.schema.admin.rbac.RbacRoleTable
import kcrud.base.database.schema.contact.ContactTable
import kcrud.base.database.schema.employee.EmployeeTable
import kcrud.base.database.schema.employment.EmploymentTable

/**
 * Configures the custom [DbPlugin].
 *
 * This will set up and configure database, including the connection pool, and register
 * the database schema tables so that the ORM can interact with them.
 *
 * See: [DbPlugin]
 */
fun Application.configureDatabase() {

    install(plugin = DbPlugin) {
        // Default admin tables.
        addTable(table = RbacFieldRuleTable)
        addTable(table = RbacResourceRuleTable)
        addTable(table = RbacRoleTable)
        addTable(table = ActorTable)

        // Domain tables.
        addTable(table = ContactTable)
        addTable(table = EmployeeTable)
        addTable(table = EmploymentTable)
    }
}
