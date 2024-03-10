/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.plugins

import io.ktor.server.application.*
import kcrud.base.admin.actor.service.DefaultActorFactory
import kcrud.base.admin.rbac.service.RbacService
import kcrud.base.database.plugin.DatabasePlugin
import kcrud.base.database.schema.admin.actor.ActorTable
import kcrud.base.database.schema.admin.rbac.RbacFieldRuleTable
import kcrud.base.database.schema.admin.rbac.RbacResourceRuleTable
import kcrud.base.database.schema.admin.rbac.RbacRoleTable
import kcrud.base.database.schema.contact.ContactTable
import kcrud.base.database.schema.employee.EmployeeTable
import kcrud.base.database.schema.employment.EmploymentTable
import kcrud.base.security.service.CredentialService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.ktor.ext.inject

/**
 * Configures the custom [DatabasePlugin].
 *
 * This will set up and configure database, including the connection pool, and register
 * the database schema tables so that the ORM can interact with them.
 *
 * See: [DatabasePlugin]
 */
fun Application.configureDatabase() {

    install(plugin = DatabasePlugin) {
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

    // Refresh the Credentials and RBAC services on application start,
    // so the caches are up-to-date and ready to handle requests.
    CoroutineScope(Dispatchers.IO).launch {

        // Ensure the database has any Actors, if none exist then create the default ones.
        DefaultActorFactory.verify()

        launch {
            val credentialService: CredentialService by inject()
            credentialService.refresh()
        }

        launch {
            val rbacService: RbacService by inject()
            rbacService.refresh()
        }
    }
}
