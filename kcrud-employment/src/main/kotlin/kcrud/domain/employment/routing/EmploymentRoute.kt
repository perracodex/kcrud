/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.domain.employment.routing

import io.ktor.server.routing.*
import kcrud.access.rbac.plugin.withRbac
import kcrud.base.database.schema.admin.rbac.types.RbacAccessLevel
import kcrud.base.database.schema.admin.rbac.types.RbacResource
import kcrud.domain.employment.routing.annotation.EmploymentRouteAPI
import kcrud.domain.employment.routing.endpoints.*

/**
 * Employment endpoints.
 *
 * These endpoints are segmented in multiple functions/files
 * to demonstrate how to organize routes separately.
 *
 * See [Application Structure](https://ktor.io/docs/structuring-applications.html) for examples
 * of how to organize routes in diverse ways.
 */
@OptIn(EmploymentRouteAPI::class)
fun Route.employmentRoute() {

    route("v1/employees/{employee_id}/employments") {

        withRbac(resource = RbacResource.EMPLOYMENT_RECORDS, accessLevel = RbacAccessLevel.VIEW) {
            findEmploymentByEmployeeId()
        }

        withRbac(resource = RbacResource.EMPLOYMENT_RECORDS, accessLevel = RbacAccessLevel.FULL) {
            createEmployment()
            deleteEmploymentByEmployeeId()
        }

        route("{employment_id}") {

            withRbac(resource = RbacResource.EMPLOYMENT_RECORDS, accessLevel = RbacAccessLevel.VIEW) {
                findEmploymentById()
            }

            withRbac(resource = RbacResource.EMPLOYMENT_RECORDS, accessLevel = RbacAccessLevel.FULL) {
                updateEmploymentById()
                deleteEmploymentById()
            }
        }
    }
}
