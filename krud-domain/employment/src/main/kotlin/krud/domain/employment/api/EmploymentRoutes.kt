/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package krud.domain.employment.api

import io.ktor.server.routing.*
import krud.access.domain.rbac.plugin.withRbac
import krud.database.schema.admin.rbac.type.RbacAccessLevel
import krud.database.schema.admin.rbac.type.RbacScope
import krud.domain.employment.api.delete.deleteEmploymentByEmployeeIdRoute
import krud.domain.employment.api.delete.deleteEmploymentByIdRoute
import krud.domain.employment.api.fetch.findEmploymentByEmployeeIdRoute
import krud.domain.employment.api.fetch.findEmploymentByIdRoute
import krud.domain.employment.api.operate.createEmploymentRoute
import krud.domain.employment.api.operate.updateEmploymentByIdRoute

/**
 * Employment endpoints.
 *
 * These endpoints are segmented in multiple functions/files
 * to demonstrate how to organize routes separately.
 *
 * #### References
 * - [Application Structure](https://ktor.io/docs/server-application-structure.html) for examples
 * of how to organize routes in diverse ways.
 */
@OptIn(EmploymentRouteApi::class)
public fun Route.employmentRoutes() {

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

/**
 * Annotation for controlled access to the Employment Routes API.
 */
@RequiresOptIn(level = RequiresOptIn.Level.ERROR, message = "Only to be used within the Employment Routes API.")
@Retention(AnnotationRetention.BINARY)
internal annotation class EmploymentRouteApi
