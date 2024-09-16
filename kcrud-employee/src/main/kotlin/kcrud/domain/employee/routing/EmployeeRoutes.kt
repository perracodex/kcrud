/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employee.routing

import io.ktor.server.routing.*
import kcrud.access.rbac.plugin.withRbac
import kcrud.base.database.schema.admin.rbac.types.RbacAccessLevel
import kcrud.base.database.schema.admin.rbac.types.RbacScope
import kcrud.domain.employee.routing.annotation.EmployeeRouteAPI
import kcrud.domain.employee.routing.endpoints.delete.deleteAllEmployeesRoute
import kcrud.domain.employee.routing.endpoints.delete.deleteEmployeeByIdRoute
import kcrud.domain.employee.routing.endpoints.get.findAllEmployeesRoute
import kcrud.domain.employee.routing.endpoints.get.findEmployeeByIdRoute
import kcrud.domain.employee.routing.endpoints.get.searchEmployeeRoute
import kcrud.domain.employee.routing.endpoints.operate.createEmployeeRoute
import kcrud.domain.employee.routing.endpoints.operate.updateEmployeeByIdRoute

/**
 * Employee related endpoints.
 *
 * See [Application Structure](https://ktor.io/docs/server-application-structure.html) for examples
 * of how to organize routes in diverse ways.
 */
@OptIn(EmployeeRouteAPI::class)
public fun Route.employeeRoute() {
    withRbac(scope = RbacScope.EMPLOYEE_RECORDS, accessLevel = RbacAccessLevel.FULL) {
        createEmployeeRoute()
        updateEmployeeByIdRoute()

        deleteAllEmployeesRoute()
        deleteEmployeeByIdRoute()
    }

    withRbac(scope = RbacScope.EMPLOYEE_RECORDS, accessLevel = RbacAccessLevel.VIEW) {
        findAllEmployeesRoute()
        findEmployeeByIdRoute()
        searchEmployeeRoute()
    }
}
