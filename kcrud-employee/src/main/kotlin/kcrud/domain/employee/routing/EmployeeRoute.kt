/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employee.routing

import io.ktor.server.routing.*
import kcrud.access.rbac.plugin.withRbac
import kcrud.base.database.schema.admin.rbac.types.RbacAccessLevel
import kcrud.base.database.schema.admin.rbac.types.RbacScope
import kcrud.domain.employee.routing.annotation.EmployeeRouteAPI
import kcrud.domain.employee.routing.endpoints.delete.deleteAllEmployees
import kcrud.domain.employee.routing.endpoints.delete.deleteEmployeeById
import kcrud.domain.employee.routing.endpoints.get.findAllEmployees
import kcrud.domain.employee.routing.endpoints.get.findEmployeeById
import kcrud.domain.employee.routing.endpoints.get.searchEmployeeRoute
import kcrud.domain.employee.routing.endpoints.operate.createEmployee
import kcrud.domain.employee.routing.endpoints.operate.updateEmployeeById

/**
 * Employee related endpoints.
 *
 * See [Application Structure](https://ktor.io/docs/server-application-structure.html) for examples
 * of how to organize routes in diverse ways.
 */
@OptIn(EmployeeRouteAPI::class)
fun Route.employeeRoute() {

    route("v1/employees") {

        withRbac(scope = RbacScope.EMPLOYEE_RECORDS, accessLevel = RbacAccessLevel.FULL) {
            createEmployee()
            deleteAllEmployees()
        }

        withRbac(scope = RbacScope.EMPLOYEE_RECORDS, accessLevel = RbacAccessLevel.VIEW) {
            findAllEmployees()
            searchEmployeeRoute()
        }

        route("{employee_id}") {
            withRbac(scope = RbacScope.EMPLOYEE_RECORDS, accessLevel = RbacAccessLevel.VIEW) {
                findEmployeeById()
            }

            withRbac(scope = RbacScope.EMPLOYEE_RECORDS, accessLevel = RbacAccessLevel.FULL) {
                updateEmployeeById()
                deleteEmployeeById()
            }
        }
    }
}
