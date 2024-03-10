/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.domain.employee.routing

import io.ktor.server.routing.*
import kcrud.base.admin.rbac.plugin.withRbac
import kcrud.base.database.schema.admin.rbac.types.RbacAccessLevel
import kcrud.base.database.schema.admin.rbac.types.RbacResource
import kcrud.domain.employee.routing.annotation.EmployeeRouteAPI
import kcrud.domain.employee.routing.endpoints.*

/**
 * Employee related endpoints.
 *
 * See [Application Structure](https://ktor.io/docs/structuring-applications.html) for examples
 * of how to organize routes in diverse ways.
 */
@OptIn(EmployeeRouteAPI::class)
fun Route.employeeRoute() {

    route("v1/employees") {

        withRbac(resource = RbacResource.EMPLOYEE_RECORDS, accessLevel = RbacAccessLevel.FULL) {
            createEmployee()
            deleteAllEmployees()
        }

        withRbac(resource = RbacResource.EMPLOYEE_RECORDS, accessLevel = RbacAccessLevel.VIEW) {
            findAllEmployees()
            filterEmployees()
        }

        route("{employee_id}") {
            withRbac(resource = RbacResource.EMPLOYEE_RECORDS, accessLevel = RbacAccessLevel.VIEW) {
                findEmployeeById()
            }

            withRbac(resource = RbacResource.EMPLOYEE_RECORDS, accessLevel = RbacAccessLevel.FULL) {
                updateEmployeeById()
                deleteEmployeeById()
            }
        }
    }
}
