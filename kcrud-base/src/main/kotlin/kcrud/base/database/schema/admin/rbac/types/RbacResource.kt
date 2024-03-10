/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.database.schema.admin.rbac.types

/**
 * Enum to represent the resources that can be accessed.
 *
 * A resource can be any concept: a database table, a REST endpoint, a UI element, etc.
 * Is up to the designer to define what a resource is, and act accordingly when its
 * associated RBAC rule is verified.
 *
 * In conjunction with [RbacAccessLevel], this enum defines the access level to a resource.
 *
 * @see RbacAccessLevel
 */
enum class RbacResource {
    RBAC_ADMIN,
    SYSTEM,
    EMPLOYEE_RECORDS,
    EMPLOYEE_CONTACT_RECORDS,
    EMPLOYMENT_RECORDS,
}
