/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employment.routing

import io.ktor.server.routing.*
import kcrud.access.rbac.plugin.withRbac
import kcrud.base.database.schema.admin.rbac.types.RbacAccessLevel
import kcrud.base.database.schema.admin.rbac.types.RbacScope
import kcrud.domain.employment.routing.annotation.EmploymentRouteAPI
import kcrud.domain.employment.routing.endpoints.delete.deleteEmploymentByEmployeeIdRoute
import kcrud.domain.employment.routing.endpoints.delete.deleteEmploymentByIdRoute
import kcrud.domain.employment.routing.endpoints.get.findEmploymentByEmployeeIdRoute
import kcrud.domain.employment.routing.endpoints.get.findEmploymentByIdRoute
import kcrud.domain.employment.routing.endpoints.operate.createEmploymentRoute
import kcrud.domain.employment.routing.endpoints.operate.updateEmploymentByIdRoute

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
public fun Route.employmentRoute() {

    withRbac(scope = RbacScope.EMPLOYMENT_RECORDS, accessLevel = RbacAccessLevel.VIEW) {
        findEmploymentByEmployeeIdRoute()
        findEmploymentByIdRoute()
    }

    withRbac(scope = RbacScope.EMPLOYMENT_RECORDS, accessLevel = RbacAccessLevel.FULL) {
        createEmploymentRoute()
        updateEmploymentByIdRoute()

        deleteEmploymentByEmployeeIdRoute()
        deleteEmploymentByIdRoute()
    }
}
