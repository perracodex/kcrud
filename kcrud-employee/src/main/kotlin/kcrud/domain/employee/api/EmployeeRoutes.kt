/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employee.api

import io.ktor.server.routing.*
import kcrud.access.rbac.plugin.withRbac
import kcrud.core.database.schema.admin.rbac.types.RbacAccessLevel
import kcrud.core.database.schema.admin.rbac.types.RbacScope
import kcrud.domain.employee.api.delete.deleteAllEmployeesRoute
import kcrud.domain.employee.api.delete.deleteEmployeeByIdRoute
import kcrud.domain.employee.api.fetch.filterEmployeeRoute
import kcrud.domain.employee.api.fetch.findAllEmployeesRoute
import kcrud.domain.employee.api.fetch.findEmployeeByIdRoute
import kcrud.domain.employee.api.fetch.searchEmployeeRoute
import kcrud.domain.employee.api.operate.createEmployeeRoute
import kcrud.domain.employee.api.operate.updateEmployeeByIdRoute

/**
 * Annotation for controlled access to the Employee Routes API.
 */
@RequiresOptIn(level = RequiresOptIn.Level.ERROR, message = "Only to be used within the Employee Routes API.")
@Retention(AnnotationRetention.BINARY)
internal annotation class EmployeeRouteApi

/**
 * Employee related endpoints.
 *
 * #### References
 * - [Application Structure](https://ktor.io/docs/server-application-structure.html) for examples
 * of how to organize routes in diverse ways.
 */
@OptIn(EmployeeRouteApi::class)
public fun Route.employeeRoutes() {
    withRbac(scope = RbacScope.EMPLOYEE_RECORDS, accessLevel = RbacAccessLevel.FULL) {
        createEmployeeRoute()
        updateEmployeeByIdRoute()

        deleteAllEmployeesRoute()
        deleteEmployeeByIdRoute()
    }

    withRbac(scope = RbacScope.EMPLOYEE_RECORDS, accessLevel = RbacAccessLevel.VIEW) {
        findAllEmployeesRoute()
        findEmployeeByIdRoute()
        filterEmployeeRoute()
        searchEmployeeRoute()
    }
}
