/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employment.routing

import io.ktor.server.routing.*
import kcrud.access.rbac.plugin.withRbac
import kcrud.base.database.schema.admin.rbac.types.RbacAccessLevel
import kcrud.base.database.schema.admin.rbac.types.RbacScope
import kcrud.domain.employment.routing.annotation.EmploymentRouteAPI
import kcrud.domain.employment.routing.endpoints.delete.deleteEmploymentByEmployeeId
import kcrud.domain.employment.routing.endpoints.delete.deleteEmploymentById
import kcrud.domain.employment.routing.endpoints.get.findEmploymentByEmployeeId
import kcrud.domain.employment.routing.endpoints.get.findEmploymentById
import kcrud.domain.employment.routing.endpoints.operate.createEmployment
import kcrud.domain.employment.routing.endpoints.operate.updateEmploymentById

/**
 * Employment endpoints.
 *
 * These endpoints are segmented in multiple functions/files
 * to demonstrate how to organize routes separately.
 *
 * See [Application Structure](https://ktor.io/docs/server-application-structure.html) for examples
 * of how to organize routes in diverse ways.
 */
@OptIn(EmploymentRouteAPI::class)
fun Route.employmentRoute() {

    route("v1/employees/{employee_id}/employments") {

        withRbac(scope = RbacScope.EMPLOYMENT_RECORDS, accessLevel = RbacAccessLevel.VIEW) {
            findEmploymentByEmployeeId()
        }

        withRbac(scope = RbacScope.EMPLOYMENT_RECORDS, accessLevel = RbacAccessLevel.FULL) {
            createEmployment()
            deleteEmploymentByEmployeeId()
        }

        route("{employment_id}") {

            withRbac(scope = RbacScope.EMPLOYMENT_RECORDS, accessLevel = RbacAccessLevel.VIEW) {
                findEmploymentById()
            }

            withRbac(scope = RbacScope.EMPLOYMENT_RECORDS, accessLevel = RbacAccessLevel.FULL) {
                updateEmploymentById()
                deleteEmploymentById()
            }
        }
    }
}
